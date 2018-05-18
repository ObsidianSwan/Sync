package com.example.finalyearproject.hollyboothroyd.sync.UI;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by hollyboothroyd
 * 12/8/2017.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private View view;
    private LayoutInflater layoutInflater;
    private Context context;
    private HashMap<String, String> imageMarkerMap;

    public CustomInfoWindow(Context context) {
        this.context = context;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.custom_info_window, null);

        imageMarkerMap = new HashMap<>();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        ImageView personImage = (ImageView) view.findViewById(R.id.userPhoto);
        if (!imageMarkerMap.isEmpty() && imageMarkerMap.containsKey(marker.getId())) {
            Picasso.with(context).load(imageMarkerMap.get(marker.getId())).into(personImage, new MarkerCallback(context, marker));
        }

        TextView name = (TextView) view.findViewById(R.id.userName);
        name.setText(marker.getTitle());

        TextView details = (TextView) view.findViewById(R.id.details);
        details.setText(marker.getSnippet());

        return view;
    }

    public void addMarkerImage(String id, String url) {
        imageMarkerMap.put(id, url);
    }
}

// Use a callback to re-render the info window once the image has been downloaded
class MarkerCallback implements Callback {
    private static final String TAG = "MarkerCallback";

    Marker mMarker;
    Context mContext;

    MarkerCallback(Context context, Marker marker) {
        this.mContext = context;
        this.mMarker = marker;
    }

    @Override
    public void onError() {
        Log.e(TAG, mContext.getString(R.string.info_window_image_download_error));
    }

    @Override
    public void onSuccess() {
        if (mMarker != null && mMarker.isInfoWindowShown()) {
            mMarker.hideInfoWindow();
            mMarker.showInfoWindow();
        }
    }
}