package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Person} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyConnectionRecyclerViewAdapter extends RecyclerView.Adapter<MyConnectionRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "MyConnectionRVA";

    private final Context mContext;
    private final List<Person> mValues;
    private final OnListFragmentInteractionListener mListener;

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;
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
        // Populate holder with connection information
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
                // Open connection popup
                connectionPopupCreation(holder);
            }
        });
    }

    private void connectionPopupCreation(final ViewHolder holder) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.person_popup, null);
        final Person person = holder.mItem;

        // Set up UI
        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView personName = (TextView) view.findViewById(R.id.popup_name);
        TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
        TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
        TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
        final Button disconnectButton = (Button) view.findViewById(R.id.popup_button);
        disconnectButton.setText(R.string.disconnect_button);

        // Load person image into connection display
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
        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        // Send profile view to viewed user
        DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(person.getUserId());
        String refKey = newNotificationRef.getKey();
        Notification notification = new Notification(refKey, mAccountManager.getCurrentUser().getUid(), NotificationType.PROFILE_VIEW);
        mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, mContext.getString(R.string.send_notification_successful));
                } else {
                    Log.e(TAG, mContext.getString(R.string.send_notification_error));
                }
            }
        });

        dialogBuilder.setView(view);
        mDialog = dialogBuilder.create();
        mDialog.show();
    }

    private void deleteConnection(final ViewHolder holder, final Person person) {
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
                                // Remove connection from the UI
                                removeConnectionItem(holder.getAdapterPosition());
                                Toast.makeText(mContext, "You're no longer connected with " + person.getFirstName(), Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Log.e(TAG, mContext.getString(R.string.delete_other_user_connection_error));
                            }
                        }
                    });
                } else {
                    Log.e(TAG, mContext.getString(R.string.delete_current_user_connection_error));
                }
            }
        });

    }

    private void removeConnectionItem(int position) {
        // Remove item from UI list
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private ImageView mConnectionProfileImage;
        private TextView mConnectionNameText;
        private TextView mConnectionPositionText;
        private TextView mConnectionCompanyText;
        private TextView mConnectionIndustryText;
        private Person mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            // Set up connection UI
            mConnectionProfileImage = (ImageView) view.findViewById(R.id.connection_profile_image);
            mConnectionNameText = (TextView) view.findViewById(R.id.connection_user_name);
            mConnectionPositionText = (TextView) view.findViewById(R.id.connection_position);
            mConnectionCompanyText = (TextView) view.findViewById(R.id.connection_company);
            mConnectionIndustryText = (TextView) view.findViewById(R.id.connection_industry);
        }
    }
}
