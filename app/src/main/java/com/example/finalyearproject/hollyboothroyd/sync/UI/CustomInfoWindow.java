package com.example.finalyearproject.hollyboothroyd.sync.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hollyboothroyd on 12/8/2017.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{
    private View view;
    private LayoutInflater layoutInflater;
    private Context context;
    private HashMap<String, String> imageMarkerMap;

    public CustomInfoWindow(Context context){
        this.context = context;

        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.custom_info_window, null);

        imageMarkerMap = new HashMap<>();

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView name = (TextView) view.findViewById(R.id.userName);
        name.setText(marker.getTitle());

        // TODO: Store person and not just image.
        TextView details = (TextView) view.findViewById(R.id.details);
        details.setText(marker.getSnippet());

        ImageView personImage = (ImageView) view.findViewById(R.id.userPhoto);
        setMarkerImage(personImage, marker.getId());

        return view;
    }

    public void addMarkerImage(String id, String url){
        imageMarkerMap.put(id, url);
    }

    private void setMarkerImage(ImageView personImage, String markerId){
        if(!imageMarkerMap.isEmpty()){
            Picasso.with(context).load(imageMarkerMap.get(markerId)).into(personImage);
        }
    }
}
