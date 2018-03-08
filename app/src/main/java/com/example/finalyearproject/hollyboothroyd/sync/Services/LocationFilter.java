package com.example.finalyearproject.hollyboothroyd.sync.Services;

import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by hollyboothroyd on 3/7/2018.
 */

public class LocationFilter {

    private static LatLng randObfuscation(LatLng originalLocation, int radius){
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius/ Constants.metersPerDegree;

        // Generate two independent uniform values
        double random1 = random.nextDouble();
        double random2 = random.nextDouble();

        double w = radiusInDegrees * Math.sqrt(random1);
        double t = 2 * Math.PI * random2;

        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double adjustedX = x / Math.cos(Math.toRadians(originalLocation.latitude));

        // Calculate the new point
        double newX = adjustedX + originalLocation.longitude;
        double newY = y + originalLocation.latitude;

        return new LatLng(newY, newX);
    }

    public static LatLng nRandObfuscation(LatLng originalLocation, int radius){
        LatLng furthestLocation = originalLocation;
        double largestDistance = 0;
        for(int i = 0; i < Constants.obfuscationTrials; i++){
            LatLng randLocation = randObfuscation(originalLocation, radius);
            double distance = distanceBetweenPoints(originalLocation, randLocation);
            if(distance > largestDistance){
                largestDistance = distance;
                furthestLocation = randLocation;
            }
        }
        return furthestLocation;
    }

    private static double distanceBetweenPoints(LatLng originalLocation, LatLng obfuscatedLocation){
        double longTheta = Math.toRadians(obfuscatedLocation.longitude - originalLocation.longitude);
        double latTheta = Math.toRadians(obfuscatedLocation.latitude - originalLocation.latitude);
        double orgLatRad = Math.toRadians(originalLocation.latitude);
        double obfLatRad = Math.toRadians(obfuscatedLocation.latitude);

        double a = Math.sin(latTheta/2) * Math.sin(latTheta/2) +
                Math.cos(orgLatRad) * Math.cos(obfLatRad) *
                        Math.sin(longTheta/2) * Math.sin(longTheta/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (Constants.earthRadiusMeters * c);

        return dist;
    }
}
