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

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NotificationFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Connection;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
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
    private AccountManager mAccountManager;

    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    private Button mPopupButton;
    private TextView mConnectionPendingMessage;

    public MyNotificationRecyclerViewAdapter(Context context, List<NotificationBase> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();
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
                        acceptConnectionRequest(notification, holder);
                    }
                });
                connectionRequestViewHolder.mDenyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        denyConnectionRequest(notification, holder);
                    }
                });
                break;
            case PROFILE_VIEW:
                final ProfileViewViewHolder profileViewViewHolder = (ProfileViewViewHolder) holder;
                profileViewViewHolder.mTimeStamp.setText(Util.getTimeDifference(notification.getTimeStamp()));
                profileViewViewHolder.mViewProfileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileViewPopupCreation(notification);
                    }
                });
                profileViewViewHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    removeNotification(holder.getAdapterPosition());
                                } else {
                                    // TODO LOG
                                }
                            }
                        });
                    }
                });
                break;
            case ERROR:
                // Add any additional view items. Currently there are none
        }
        //TODO: test for error case
    }

    private void profileViewPopupCreation(final NotificationBase notification) {
        mDialogBuilder = new AlertDialog.Builder(mContext);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.person_popup, null);
        mDatabaseManager.getPersonReference(notification.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Person person = dataSnapshot.getValue(Person.class);
                if (person != null) {

                    Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
                    ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
                    TextView personName = (TextView) view.findViewById(R.id.popup_name);
                    TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
                    TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
                    TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
                    mPopupButton = (Button) view.findViewById(R.id.popup_button);
                    mConnectionPendingMessage = (TextView) view.findViewById(R.id.popup_connection_pending);

                    Picasso.with(mContext).load(person.getImageId()).into(personImage);

                    personName.setText(person.getFirstName() + " " + person.getLastName());
                    personPosition.setText("Position: " + person.getPosition());
                    personCompany.setText("Company: " + person.getCompany());
                    personIndustry.setText("Industry: " + person.getIndustry());

                    // The users are connected
                    if (UserConnections.CONNECTION_ITEM_MAP.containsKey(person.getUserId())) {
                        mPopupButton.setText(R.string.disconnect_button);
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteConnection(person);
                            }
                        });
                    } // The current user has already sent a connection request to the other user
                    else if (UserConnections.CONNECTION_REQUEST_ITEM_MAP.containsKey(person.getUserId())) {
                        // Don't show connection button if a connection is already pending
                        mPopupButton.setVisibility(View.GONE);
                        mConnectionPendingMessage.setVisibility(View.VISIBLE);
                    } // The current user has a connection request from the other user
                    else if (UserNotifications.CONNECTION_REQUEST_ITEMS_MAP.containsKey(person.getUserId())) {
                        mPopupButton.setText(R.string.accept_connection_request_button_text);
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addConnection(notification);
                            }
                        });
                    } // Users are not connected and no connection requests have been sent
                    else {
                        mPopupButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendConnectionRequest(person.getUserId());
                            }
                        });

                    }
                    dismissPopupButton.setOnClickListener(new View.OnClickListener() {
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addConnection(final NotificationBase notification) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentUserId = mAccountManager.getCurrentUser().getUid();
                // Add connection to current users database
                mDatabaseManager.addConnection(notification.getId(), currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection to other users database
                            mDatabaseManager.addConnection(currentUserId, notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Delete connection request in the other users database
                                        mDatabaseManager.deleteUserConnectionRequest(notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Delete connection notification in the current users database
                                                    mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(mContext, R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // TODO LOG
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    // TODO: LOG
                                                }
                                            }
                                        });
                                    } else {
                                        //TODO:Log
                                    }
                                }
                            });
                        } else {
                            //TODO:Log
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

    private void sendConnectionRequest(final String personId) {
        DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(personId);
        String refKey = newNotificationRef.getKey();
        Notification notification = new Notification(refKey, mAccountManager.getCurrentUser().getUid(), NotificationType.CONNECTION_REQUEST);
        // Send connection request to the other user
        mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Save reference of the request so the current user's connection requests are known
                    mDatabaseManager.addUserConnectionRequest(personId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Don't show connection button now that the connection request is pending
                                mPopupButton.setVisibility(View.GONE);
                                mConnectionPendingMessage.setVisibility(View.VISIBLE);
                                Toast.makeText(mContext, R.string.connection_request_sent_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteConnection(final Person person) {
        final String currentUserId = mAccountManager.getCurrentUser().getUid();

        // Remove the connection reference from the current users database
        mDatabaseManager.deleteConnection(currentUserId, person.getUserId()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove the connection reference from the connection users database
                    mDatabaseManager.deleteConnection(person.getUserId(), currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
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
    }

    private void acceptConnectionRequest(final NotificationBase notification, final ViewHolder holder) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentUserId = mAccountManager.getCurrentUser().getUid();
                // Add connection to current users database
                mDatabaseManager.addConnection(notification.getId(), currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection to other users database
                            mDatabaseManager.addConnection(currentUserId, notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Delete connection request in the other users database
                                        mDatabaseManager.deleteUserConnectionRequest(notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Delete connection notification in the current users database
                                                    mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                removeNotification(holder.getAdapterPosition());
                                                                Toast.makeText(mContext, R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // TODO LOG
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    // TODO: LOG
                                                }
                                            }
                                        });
                                    } else {
                                        //TODO:Log
                                    }
                                }
                            });
                        } else {
                            //TODO:Log
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

    private void denyConnectionRequest(final NotificationBase notification, final ViewHolder holder) {
        // Remove the connection request notification from the current users (requestor) database
        mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove the connection request from other users (requestee) database
                    Person person = UserConnections.CONNECTION_REQUEST_ITEM_MAP.get(notification.getId());
                    mDatabaseManager.deleteUserConnectionRequest(notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        private Button mViewProfileButton;
        private Button mRemoveButton;

        public ProfileViewViewHolder(View view) {
            super(view);
            mView = view;
            mTimeStamp = (TextView) view.findViewById(R.id.notification_timestamp_text);
            mViewProfileButton = (Button) view.findViewById(R.id.notification_profile_view_button);
            mRemoveButton = (Button) view.findViewById(R.id.notification_remove_button);
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
