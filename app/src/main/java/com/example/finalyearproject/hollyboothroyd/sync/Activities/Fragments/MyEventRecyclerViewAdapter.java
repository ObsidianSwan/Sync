package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
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
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    public MyEventRecyclerViewAdapter(Context context, List<Event> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event, parent, false);
        mDatabaseManager = new DatabaseManager();
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
                eventPopupCreation(holder.mItem);
            }
        });
    }

    private void eventPopupCreation(final Event event) {
        mDialogBuilder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_popup, null);

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView eventImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView eventTitle = (TextView) view.findViewById(R.id.popup_title);
        TextView eventTopic = (TextView) view.findViewById(R.id.popup_topic);
        TextView eventIndustry = (TextView) view.findViewById(R.id.popup_industry);
        TextView eventDate = (TextView) view.findViewById(R.id.popup_date);
        TextView eventTime = (TextView) view.findViewById(R.id.popup_time);
        TextView eventDescription = (TextView) view.findViewById(R.id.popup_description);
        Button attendButton = (Button) view.findViewById(R.id.popup_attend);
        TextView attendingMessage = (TextView) view.findViewById(R.id.popup_attending);

        if (event != null) {
            Picasso.with(mContext).load(event.getImageId()).into(eventImage);
        }

        eventTitle.setText(event.getTitle());
        eventTopic.setText("Topic: " + event.getTopic());
        eventIndustry.setText("Industry: " + event.getIndustry());
        eventDate.setText("Date: " + event.getDate());
        eventTime.setText("Time: " + event.getTime());
        eventDescription.setText(event.getDescription());

        if (UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            // Don't show attend button if the user is already attending the event
            attendButton.setVisibility(View.GONE);
            attendingMessage.setVisibility(View.VISIBLE);
        } else {
            attendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //final Event desiredEvent = event;
                    mDatabaseManager.attendNewEvent(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext, "You're attending " + event.getTitle() + "!", Toast.LENGTH_LONG).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(mContext, R.string.event_attendence_unsuccessful, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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
