package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;

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

}
