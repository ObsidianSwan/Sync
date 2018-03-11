package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ViewEventsFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Event} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyEventRecyclerViewAdapter extends RecyclerView.Adapter<MyEventRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Event> mValues;
    private final OnListFragmentInteractionListener mListener;

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    public MyEventRecyclerViewAdapter(Context context, List<Event> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Picasso.with(mContext).load(mValues.get(position).getImageId()).into(holder.mEventImage);
        holder.mEventTitle.setText(mValues.get(position).getTitle());
        holder.mEventTopic.setText(mValues.get(position).getTopic());
        holder.mEventIndustry.setText(mValues.get(position).getIndustry());
        holder.mEventDate.setText(mValues.get(position).getDate());
        holder.mEventTime.setText(mValues.get(position).getTime());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
                eventPopupCreation(holder);
            }
        });
    }

    private void eventPopupCreation(final ViewHolder holder) {
        mDialogBuilder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_popup, null);
        final Event event = holder.mItem;

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView eventImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView eventTitle = (TextView) view.findViewById(R.id.popup_title);
        TextView eventTopic = (TextView) view.findViewById(R.id.popup_topic);
        TextView eventIndustry = (TextView) view.findViewById(R.id.popup_industry);
        TextView eventDate = (TextView) view.findViewById(R.id.popup_date);
        TextView eventTime = (TextView) view.findViewById(R.id.popup_time);
        TextView eventDescription = (TextView) view.findViewById(R.id.popup_description);
        Button eventButton = (Button) view.findViewById(R.id.popup_event_button);
        Button eventButton2 = (Button) view.findViewById(R.id.popup_event_button2);

        if (event != null) {
            Picasso.with(mContext).load(event.getImageId()).into(eventImage);
        }

        eventTitle.setText(event.getTitle());
        eventTopic.setText("Topic: " + event.getTopic());
        eventIndustry.setText("Industry: " + event.getIndustry());
        eventDate.setText("Date: " + event.getDate());
        eventTime.setText("Time: " + event.getTime());
        eventDescription.setText(event.getDescription());

        // The user is both hosting and attending the event
        if(UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) &&
                UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())){
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);

            // Stop attending the event if the user is already attending
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userId = mAccountManager.getCurrentUser().getUid();
                    stopAttendingEvent(event, userId, holder);
                }
            });

            // Delete event if the user is hosting the event
            eventButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(event, holder);
                }
            });

        } // The user is hosting, but not attending the event
        else if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) && !UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())){
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);

            // Attend event
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendEvent(event);
                }
            });

            // Delete event if the user is hosting the event
            eventButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(event, holder);
                }
            });

        } // The user is attending, but not hosting the event
        else if (UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid()) &&
                !UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid())) {
            final String userId = mAccountManager.getCurrentUser().getUid();

            // Stop attending the event if the user is already attending
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAttendingEvent(event, userId, holder);
                }
            });
        } // The user is not hosting or attending the event
        else {
            // Attend event if the user is not attending
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendEvent(event);
                }
            });
        }
        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialogBuilder.setView(view);
        mDialog = mDialogBuilder.create();
        mDialog.show();
    }

    private void attendEvent(final Event event){
        mDatabaseManager.addUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabaseManager.addEventAttending(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "You're attending " + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(mContext, R.string.event_attendence_unsuccessful, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, R.string.event_attendence_unsuccessful, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void stopAttendingEvent(final Event event, final String userId, final ViewHolder holder){
        // Delete attendee from event attending database
        mDatabaseManager.deleteUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mDatabaseManager.deleteEventAttending(event.getUid(), userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                removeEvent(holder.getAdapterPosition());
                                Toast.makeText(mContext, "You're no longer attending" + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(mContext, R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else{
                    Toast.makeText(mContext, R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteEvent(final Event event, final ViewHolder holder){
        // Delete attending references in user attending database
        mDatabaseManager.getUsersAttendingEvent(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String attendeeId = snapshot.getKey();
                    mDatabaseManager.deleteEventAttending(event.getUid(), attendeeId);
                    // TODO How to check if the deletions all happened successfully
                }
                // Stop hosting the event
                mDatabaseManager.deleteEventHosting(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            // Delete event in event database
                            mDatabaseManager.deleteEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        removeEvent(holder.getAdapterPosition());
                                        Toast.makeText(mContext, R.string.delete_event_successful, Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();
                                    } else {
                                        Toast.makeText(mContext, R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(mContext, R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                            // TODO LOg
                        }
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeEvent(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mEventImage;
        private final TextView mEventTitle;
        private final TextView mEventTopic;
        private final TextView mEventIndustry;
        private final TextView mEventDate;
        private final TextView mEventTime;
        private Event mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEventImage = (ImageView) view.findViewById(R.id.event_image);
            mEventTitle = (TextView) view.findViewById(R.id.event_title_text);
            mEventTopic = (TextView) view.findViewById(R.id.event_topic_text);
            mEventIndustry = (TextView) view.findViewById(R.id.event_industry_text);
            mEventDate = (TextView) view.findViewById(R.id.event_date_text);
            mEventTime = (TextView) view.findViewById(R.id.event_time_text);
        }
    }
}
