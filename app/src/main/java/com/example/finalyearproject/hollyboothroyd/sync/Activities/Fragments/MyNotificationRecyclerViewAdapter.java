package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

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

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NotificationFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Connection;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link NotificationBase} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyNotificationRecyclerViewAdapter extends RecyclerView.Adapter<MyNotificationRecyclerViewAdapter.ViewHolder> {

    private static final int NOTIFICATION_ERROR_TYPE = 0;
    private static final int NOTIFICATION_CONNECTION_REQUEST_TYPE = 1;
    private static final int NOTIFICATION_PROFILE_VIEWED_TYPE = 2;

    private final Context mContext;
    private final List<NotificationBase> mValues;
    private final OnListFragmentInteractionListener mListener;

    private DatabaseManager mDatabaseManager;

    public MyNotificationRecyclerViewAdapter(Context context, List<NotificationBase> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDatabaseManager = new DatabaseManager();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mValues.get(position).getType()) {
            case CONNECTION_REQUEST:
                return NOTIFICATION_CONNECTION_REQUEST_TYPE;
            case PROFILE_VIEW:
                return NOTIFICATION_PROFILE_VIEWED_TYPE;
        }
        return NOTIFICATION_ERROR_TYPE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_notification_error, parent, false);
                return new ErrorViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_notification_connection_request, parent, false);
                return new ConnectionRequestViewHolder(view);

            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_notification_profile_view, parent, false);
                return new ProfileViewViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final NotificationBase notification = mValues.get(position);
        Picasso.with(mContext).load(notification.getImageId()).into(holder.mNotificationImage);
        holder.mNotificationDescription.setText(notification.getDescription());

        switch (notification.getType()) {
            case CONNECTION_REQUEST:
                //TODO: error checking
                final ConnectionRequestViewHolder connectionRequestViewHolder = (ConnectionRequestViewHolder) holder;
                connectionRequestViewHolder.mTimeStamp.setText(Util.getTimeDifference(notification.getTimeStamp()));
                connectionRequestViewHolder.mConfirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Person user = dataSnapshot.getValue(Person.class);
                                // First add the other user (requestee) to current users (requestor) connection list
                                DatabaseReference connectionRef = mDatabaseManager.getNewConnectionReference(user.getUserId());
                                String dbRef = connectionRef.getKey();
                                Connection connection = new Connection(dbRef, notification.getId());
                                mDatabaseManager.addUserConnection(connectionRef, connection).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Then add the current user (requestor) to the other users (requestee) connection list
                                            DatabaseReference connectionRef = mDatabaseManager.getNewConnectionReference(notification.getId());
                                            String dbRef = connectionRef.getKey();
                                            Connection connection = new Connection(dbRef, user.getUserId());
                                            mDatabaseManager.addUserConnection(connectionRef, connection).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Remove the connection request notification from the current users (requestor) database
                                                        mDatabaseManager.deleteUserConnectionRequestNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    // Remove the connection request from other users (requestee) database
                                                                    Connection connection = UserConnections.CONNECTION_REQUEST_ITEM_MAP.get(notification.getId());
                                                                    mDatabaseManager.deleteUserConnectionRequest(connection.getConnectionDbRef(), notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                // Update the UI to remove the notification in the list
                                                                                removeNotification(holder.getAdapterPosition());
                                                                                Toast.makeText(mContext, R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Toast.makeText(mContext, R.string.generic_error_text, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(mContext, R.string.generic_error_text, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(mContext, R.string.generic_error_text, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //TODO:Log
                            }
                        });
                    }
                });
                connectionRequestViewHolder.mDenyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove the connection request notification from the current users (requestor) database
                        mDatabaseManager.deleteUserConnectionRequestNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Remove the connection request from other users (requestee) database
                                    Connection connection = UserConnections.CONNECTION_REQUEST_ITEM_MAP.get(notification.getId());
                                    mDatabaseManager.deleteUserConnectionRequest(connection.getConnectionDbRef(), notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                removeNotification(holder.getAdapterPosition());
                                                Toast.makeText(mContext, R.string.connection_denied_toast_text, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(mContext, R.string.generic_error_text, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                break;
            case PROFILE_VIEW:
                ProfileViewViewHolder profileViewViewHolder = (ProfileViewViewHolder) holder;
                profileViewViewHolder.mTimeStamp.setText(notification.getTimeStamp().toString());
                break;
            case ERROR:
                // Add any additional view items. Currently there are none
        }
        //TODO: test for error case
    }

    private void removeNotification(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private final ImageView mNotificationImage;
        private final TextView mNotificationTitle;
        private final TextView mNotificationDescription;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNotificationImage = (ImageView) view.findViewById(R.id.notification_image);
            mNotificationTitle = (TextView) view.findViewById(R.id.notification_title_text);
            mNotificationDescription = (TextView) view.findViewById(R.id.notification_description_text);
        }
    }

    public class ConnectionRequestViewHolder extends ViewHolder {
        public final View mView;
        private TextView mTimeStamp;
        private Button mConfirmButton;
        private Button mDenyButton;

        public ConnectionRequestViewHolder(View view) {
            super(view);
            mView = view;
            mTimeStamp = (TextView) view.findViewById(R.id.notification_timestamp_text);
            mConfirmButton = (Button) view.findViewById(R.id.notification_connection_accept_button);
            mDenyButton = (Button) view.findViewById(R.id.notification_connection_deny_button);
        }
    }

    public class ProfileViewViewHolder extends ViewHolder {
        public final View mView;
        private TextView mTimeStamp;

        public ProfileViewViewHolder(View view) {
            super(view);
            mView = view;
            mTimeStamp = (TextView) view.findViewById(R.id.notification_timestamp_text);
        }
    }

    public class ErrorViewHolder extends ViewHolder {
        public final View mView;
        private NotificationBase mItem;

        public ErrorViewHolder(View view) {
            super(view);
            mView = view;
        }
    }
}
