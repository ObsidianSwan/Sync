package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by hollyboothroyd on 12/10/2017.
 */

public class Util {
    public static String getMapKey(HashMap map, float value) {
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
}
