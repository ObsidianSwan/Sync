package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private OnFragmentInteractionListener mListener;

    private final HashMap<String, Float> pinColorMap = new HashMap<String, Float>();

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferenceEditor;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = getActivity().getApplicationContext().getSharedPreferences(Constants.preferences, MODE_PRIVATE);
        mPreferenceEditor = mPreferences.edit();

        // Populate pinColorMap
        pinColorMap.put("Azure", BitmapDescriptorFactory.HUE_AZURE);
        pinColorMap.put("Blue", BitmapDescriptorFactory.HUE_BLUE);
        pinColorMap.put("Cyan", BitmapDescriptorFactory.HUE_CYAN);
        pinColorMap.put("Green", BitmapDescriptorFactory.HUE_GREEN);
        pinColorMap.put("Magenta", BitmapDescriptorFactory.HUE_MAGENTA);
        pinColorMap.put("Orange", BitmapDescriptorFactory.HUE_ORANGE);
        pinColorMap.put("Red", BitmapDescriptorFactory.HUE_RED);
        pinColorMap.put("Rose", BitmapDescriptorFactory.HUE_ROSE);
        pinColorMap.put("Violet", BitmapDescriptorFactory.HUE_VIOLET);
        pinColorMap.put("Yellow", BitmapDescriptorFactory.HUE_YELLOW);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        getActivity().setTitle(R.string.settings_action_bar_title);

        NumberPicker zoomPicker = (NumberPicker) view.findViewById(R.id.settings_map_zoom_number_picker);
        NumberPicker timeRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_time_refresh_number_picker);
        NumberPicker disRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_dis_refresh_number_picker);
        Spinner personPinColorSpinner = (Spinner) view.findViewById(R.id.settings_user_color_spinner);
        Spinner eventPinColorSpinner = (Spinner) view.findViewById(R.id.settings_event_color_spinner);

        zoomPicker.setMinValue(Constants.zoomPickerMinValue);
        zoomPicker.setMaxValue(Constants.zoomPickerMaxValue);
        zoomPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mPreferenceEditor.putInt(Constants.mapZoomLevelName, newVal);
                mPreferenceEditor.commit();
            }
        });

        timeRefreshRatePicker.setMinValue(Constants.timeRefreshRatePickerMinValue);
        timeRefreshRatePicker.setMaxValue(Constants.timeRefreshRatePickerMaxValue);
        timeRefreshRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mPreferenceEditor.putInt(Constants.locationTimeUpdateIntervalName, newVal);
                mPreferenceEditor.commit();
            }
        });

        disRefreshRatePicker.setMinValue(Constants.disRefreshRatePickerMinValue);
        disRefreshRatePicker.setMaxValue(Constants.disRefreshRatePickerMaxValue);
        disRefreshRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mPreferenceEditor.putInt(Constants.locationDistanceUpdateIntervalName, newVal);
                mPreferenceEditor.commit();
            }
        });

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pin_color_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personPinColorSpinner.setAdapter(arrayAdapter);
        personPinColorSpinner.setOnItemSelectedListener(this);
        eventPinColorSpinner.setAdapter(arrayAdapter);
        eventPinColorSpinner.setOnItemSelectedListener(this);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        float color = pinColorMap.get(spinner.getItemAtPosition(position));
        if(spinner.getId() == R.id.settings_user_color_spinner)
        {
            mPreferenceEditor.putFloat(Constants.personPinColorName, color);
            mPreferenceEditor.commit();
        }
        else if(spinner.getId() == R.id.settings_event_color_spinner)
        {
            mPreferenceEditor.putFloat(Constants.eventPinColorName, color);
            mPreferenceEditor.commit();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
