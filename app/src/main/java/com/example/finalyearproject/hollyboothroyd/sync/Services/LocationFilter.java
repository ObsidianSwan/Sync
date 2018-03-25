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

        // Calculate a new random radius that is lower than the specified radius
        double newRadius = radiusInDegrees * Math.sqrt(random1);

        // Calculate a new random angle that is between 0 and 2pi radians
        double newAngle = 2 * Math.PI * random2;

        // Find the x and y points given the radius and angle
        double x = newRadius * Math.cos(newAngle);
        double y = newRadius * Math.sin(newAngle);

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

    public static boolean eventWithinRange(LatLng originalLocation, LatLng eventLocation){
        // TODO change for settings page.
        if(distanceBetweenPoints(originalLocation, eventLocation) < Constants.geofenceRadiusDefault){
            return true;
        }
        return false;
    }

    // Returns the distance between two LatLng positions in meters using the Haversine formula
    private static double distanceBetweenPoints(LatLng originalLocation, LatLng secondaryLocation){
        double longTheta = Math.toRadians(originalLocation.longitude - secondaryLocation.longitude);
        double latTheta = Math.toRadians(originalLocation.latitude - secondaryLocation.latitude);
        double orgLatRad = Math.toRadians(originalLocation.latitude);
        double secLatRad = Math.toRadians(secondaryLocation.latitude);

        // Calculate the central angle
        double angle = Math.sin(latTheta/2) * Math.sin(latTheta/2) +
                Math.cos(orgLatRad) * Math.cos(secLatRad) *
                        Math.sin(longTheta/2) * Math.sin(longTheta/2);

        // Begin conversion from the central angle degrees to distance meters
        // by applying the inverse haversine
        double c = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));

        // Return the float distance in meters between two points
        return (float) (Constants.earthRadiusMeters * c);
    }
}
