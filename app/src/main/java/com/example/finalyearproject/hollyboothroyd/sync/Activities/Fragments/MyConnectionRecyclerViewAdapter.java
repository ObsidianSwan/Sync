package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment.OnListFragmentInteractionListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
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

    public MyConnectionRecyclerViewAdapter(Context context, List<Person> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
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
            }
        });
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
