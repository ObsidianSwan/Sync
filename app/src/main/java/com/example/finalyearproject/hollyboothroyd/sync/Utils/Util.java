package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by hollyboothroyd on 12/10/2017.
 */

public class Util {

    private static AlertDialog mDialog;

    private static final Pattern sDatePattern =
            Pattern.compile("^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$");
    private static final Pattern sTimePattern =
            Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");

    public static String getMapKeyFloat(HashMap map, float value) {
        for (Object entry : map.keySet()) {
            if (map.get(entry).equals(value)) {
                return (String) entry;
            }
        }
        return null;
    }

    public static String getMapKeyInt(HashMap map, int value) {
        for (Object entry : map.keySet()) {
            if (map.get(entry).equals(value)) {
                return (String) entry;
            }
        }
        return null;
    }

    public static String getTimeDifference(Date timestamp){
        Date currentDateTime = new Date();

        long duration  = currentDateTime.getTime() - timestamp.getTime();

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);

        if(diffInMinutes > 60){
            if(diffInHours > 24){
                return String.format ("%dd", diffInDays);
            } else {
                return String.format ("%dh", diffInHours);
            }
        }
        return String.format ("%dm", diffInMinutes);
    }

    @SuppressLint("MissingPermission")
    public static Location getLastKnownLocation(LocationManager locationManager) {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public static LatLng getLocationFromAddress(Context context, String inputtedAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng position = null;

        try {
            // Get location coordinates from the user inputted address
            // May throw an IOException
            address = coder.getFromLocationName(inputtedAddress, 5);
            if (address.isEmpty()) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            position = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return position;
    }

    public static boolean isTimeValid(String time) {
        return sTimePattern.matcher(time).matches();
    }

    public static boolean isDateValid(String date) {
        // Verify date is in the valid format
        if(!sDatePattern.matcher(date).matches()){
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}