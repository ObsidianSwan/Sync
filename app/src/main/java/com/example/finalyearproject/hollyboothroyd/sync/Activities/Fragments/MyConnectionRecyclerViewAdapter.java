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

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Connection;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Person} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyConnectionRecyclerViewAdapter extends RecyclerView.Adapter<MyConnectionRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Person> mValues;
    private final OnListFragmentInteractionListener mListener;

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;


    public MyConnectionRecyclerViewAdapter(Context context, List<Person> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_connection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Picasso.with(mContext).load(mValues.get(position).getImageId()).into(holder.mConnectionProfileImage);
        String connectionName = mValues.get(position).getFirstName() + " " + mValues.get(position).getLastName();
        holder.mConnectionNameText.setText(connectionName);
        holder.mConnectionPositionText.setText(mValues.get(position).getPosition());
        holder.mConnectionCompanyText.setText(mValues.get(position).getCompany());
        holder.mConnectionIndustryText.setText(mValues.get(position).getIndustry());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
                connectionPopupCreation(holder);
            }
        });
    }

    private void connectionPopupCreation(final ViewHolder holder) {
        mDialogBuilder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.person_popup, null);
        final Person person = holder.mItem;

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView personName = (TextView) view.findViewById(R.id.popup_name);
        TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
        TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
        TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
        final Button disconnectButton = (Button) view.findViewById(R.id.popup_button);
        disconnectButton.setText(R.string.disconnect_button);

        if (person != null) {
            Picasso.with(mContext).load(person.getImageId()).into(personImage);
        }

        personName.setText(person.getFirstName() + " " + person.getLastName());
        personPosition.setText("Position: " + person.getPosition());
        personCompany.setText("Company: " + person.getCompany());
        personIndustry.setText("Industry: " + person.getIndustry());

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConnection(holder, person);
            }
        });
        dismissPopupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(person.getUserId());
        String refKey = newNotificationRef.getKey();
        Notification notification = new Notification(refKey, mAccountManager.getCurrentUser().getUid(), NotificationType.PROFILE_VIEW);
        mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //TODO log
                } else {
                    //TODO log
                }
            }
        });

        mDialogBuilder.setView(view);
        mDialog = mDialogBuilder.create();
        mDialog.show();
    }

    private void deleteConnection(final ViewHolder holder, final Person person){
        // Get the connection database reference from the connectionId
        mDatabaseManager.getUserConnectionReference(person.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dbRef = dataSnapshot.getValue(String.class);
                // Remove the connection from the connection database
                mDatabaseManager.deleteConnection(dbRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Remove the connection reference from the current users database
                            mDatabaseManager.deleteUserConnection(mAccountManager.getCurrentUser().getUid(), person.getUserId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mDatabaseManager.deleteUserConnection(person.getUserId(), mAccountManager.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    removeConnection(holder.getAdapterPosition());
                                                    Toast.makeText(mContext, "You're no longer connected with " + person.getFirstName(), Toast.LENGTH_SHORT).show();
                                                    mDialog.dismiss();
                                                } else {
                                                    // TODO: Log
                                                }
                                            }
                                        });
                                    } else {
                                        // TODO: Log
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(mContext, R.string.cannot_disconnect_toast_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeConnection(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public ImageView mConnectionProfileImage;
        public TextView mConnectionNameText;
        public TextView mConnectionPositionText;
        public TextView mConnectionCompanyText;
        public TextView mConnectionIndustryText;
        public Person mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mConnectionProfileImage = (ImageView) view.findViewById(R.id.connection_profile_image);
            mConnectionNameText = (TextView) view.findViewById(R.id.connection_user_name);
            mConnectionPositionText = (TextView) view.findViewById(R.id.connection_position);
            mConnectionCompanyText = (TextView) view.findViewById(R.id.connection_company);
            mConnectionIndustryText = (TextView) view.findViewById(R.id.connection_industry);
        }
    }
}
