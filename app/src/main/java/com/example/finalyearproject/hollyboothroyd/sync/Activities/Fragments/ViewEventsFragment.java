package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;

/**
 * A fragment representing a list of Event items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ViewEventsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private RecyclerView mAllEvents;
    private RecyclerView mEventsAttending;
    private RecyclerView mEventsHosting;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ViewEventsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_view_events, container, false);

        // Set up UI
        getActivity().setTitle(R.string.view_events_action_bar_title);

        mAllEvents = (RecyclerView) view.findViewById(R.id.all_events_recycler_view);
        mEventsAttending = (RecyclerView) view.findViewById(R.id.events_attending_recycler_view);
        mEventsHosting = (RecyclerView) view.findViewById(R.id.events_hosting_recycler_view);

        FragmentTabHost tabHost = (FragmentTabHost) view.findViewById(R.id.tab_host);
        tabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_content);

        // Set up All Events tab
        tabHost.addTab(tabHost.newTabSpec(Constants.allEventsTab).setIndicator(Constants.allEventsTabName).setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                Context context = view.getContext();
                if (mColumnCount <= 1) {
                    mAllEvents.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mAllEvents.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                // Populate tab with all events list
                mAllEvents.setAdapter(new MyEventRecyclerViewAdapter(getActivity(), UserEvents.ALL_EVENTS, mListener));

                return mAllEvents;
            }
        }));

        // Set up Events Attending tab
        tabHost.addTab(tabHost.newTabSpec(Constants.eventsAttendingTab).setIndicator(Constants.eventsAttendingTabName).setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                Context context = view.getContext();
                if (mColumnCount <= 1) {
                    mEventsAttending.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mEventsAttending.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                // Populate tab with events attending list
                mEventsAttending.setAdapter(new MyEventRecyclerViewAdapter(getActivity(), UserEvents.EVENTS_ATTENDING, mListener));

                return mEventsAttending;
            }
        }));

        // Set up Events Hosting tab
        tabHost.addTab(tabHost.newTabSpec(Constants.eventsHostingTab).setIndicator(Constants.eventsHostingTabName).setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                Context context = view.getContext();
                if (mColumnCount <= 1) {
                    mEventsHosting.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mEventsHosting.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                // Populate tab with events hosting list
                mEventsHosting.setAdapter(new MyEventRecyclerViewAdapter(getActivity(), UserEvents.EVENTS_HOSTING, mListener));

                return mEventsHosting;
            }
        }));

        tabHost.setCurrentTab(2);
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(0);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Event item);
    }
}
