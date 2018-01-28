package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.example.finalyearproject.hollyboothroyd.sync.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewEventBasicInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewEventBasicInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewEventBasicInfoFragment extends Fragment {

    private EditText mEventTitleText;
    private EditText mEventIndustryText;
    private EditText mEventTopicText;
    private Button mNextButton;

    private OnFragmentInteractionListener mListener;

    public NewEventBasicInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewEventBasicInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewEventBasicInfoFragment newInstance() {
        NewEventBasicInfoFragment fragment = new NewEventBasicInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_basic_info, container, false);

        mEventTitleText = (EditText) view.findViewById(R.id.new_event_title_text);
        mEventIndustryText = (EditText) view.findViewById(R.id.new_event_industry_text);
        mEventTopicText = (EditText) view.findViewById(R.id.new_event_topic_text);
        mNextButton = (Button) view.findViewById(R.id.event_basic_info_next_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventTitle = mEventTitleText.getText().toString();
                String eventIndustry = mEventIndustryText.getText().toString();
                String eventTopic = mEventTopicText.getText().toString();

                if(areEntriesValid(eventTitle, eventIndustry, eventTopic)) {
                    if (mListener != null) {
                        mListener.onNewEventInfoNextButtonPressed(eventTitle, eventIndustry, eventTopic);
                    }
                }
            }
        });
        return view;
    }

    private boolean areEntriesValid(String eventTitle, String eventIndustry, String eventTopic) {
        // Reset errors.
        mEventTitleText.setError(null);
        mEventIndustryText.setError(null);
        mEventTopicText.setError(null);

        View focusView = null;

        // Check for a valid event title
        if (TextUtils.isEmpty(eventTitle)) {
            mEventTitleText.setError(getString(R.string.error_field_required));
            focusView = mEventTitleText;
        }
        // Check for a valid event industry
        if (TextUtils.isEmpty(eventIndustry)) {
            mEventIndustryText.setError(getString(R.string.error_field_required));
            focusView = mEventIndustryText;
        }
        // Check for a valid event topic
        if (TextUtils.isEmpty(eventTopic)) {
            mEventTopicText.setError(getString(R.string.error_field_required));
            focusView = mEventTopicText;
        }

        if (focusView != null) {
            // There was an error; don't attempt go to the next fragment and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }

        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onNewEventInfoNextButtonPressed(String eventTitle, String eventIndustry, String eventTopic);
    }
}
