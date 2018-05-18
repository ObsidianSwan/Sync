package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by hollyboothroyd
 * 12/10/2017.
 */

public class Util {

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
                return String.format (Locale.getDefault(),"%dd", diffInDays);
            } else {
                return String.format (Locale.getDefault(),"%dh", diffInHours);
            }
        }
        return String.format (Locale.getDefault(),"%dm", diffInMinutes);
    }

    // Permissions check is done in the calling activity or fragment
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}