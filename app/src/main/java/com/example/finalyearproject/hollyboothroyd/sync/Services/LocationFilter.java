package com.example.finalyearproject.hollyboothroyd.sync.Services;

import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by hollyboothroyd on 3/7/2018.
 */

public class LocationFilter {

    public static LatLng randObfuscation(LatLng originalLocation, int radius){
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

        // Convert the polar coordinates to cartesian coordinates and return
        return convertPolarToCartesianPoints(originalLocation, newRadius, newAngle);
    }

    public static LatLng nRandObfuscation(LatLng originalLocation, int radius){
        LatLng furthestLocation = originalLocation;
        double largestDistance = 0;

        // For the desired obfuscation trials (4) calculate the rand obfuscated point
        for(int i = 0; i < Constants.obfuscationTrials; i++){

            // Calculate the rand obfuscated point
            LatLng randLocation = randObfuscation(originalLocation, radius);

            // Calculate the distance between the original and obfuscated point
            double distance = distanceBetweenPoints(originalLocation, randLocation);

            // Check if the obfuscated point is the furthest point
            if(distance > largestDistance){

                // Save the largest distance and furthest point
                largestDistance = distance;
                furthestLocation = randLocation;
            }
        }

        // Return the furthest point
        return furthestLocation;
    }

    public static LatLng thetaRandObfuscation(LatLng originalLocation, int radius){
        Random random = new Random();

        // Generate two independent uniform values
        double random1 = random.nextDouble();
        double random2 = random.nextDouble();

        // Calculate two new random angles that are between 0 and 2pi radians
        double newAngle1 = 2 * Math.PI * random1;
        double newAngle2 = 2 * Math.PI * random2;

        LatLng furthestLocation = originalLocation;
        double largestDistance = 0;

        // For the desired obfuscation trials (4) calculate the theta-rand obfuscated point
        for(int i = 0; i < Constants.obfuscationTrials; i++){

            // Calculate the theta-rand obfuscated point
            LatLng randLocation = thetaRandImpl(originalLocation, radius, Math.min(newAngle1, newAngle2), Math.max(newAngle1, newAngle2));

            // Check if the obfuscated point is the furthest point
            double distance = distanceBetweenPoints(originalLocation, randLocation);
            if(distance > largestDistance){

                // Save the largest distance and furthest point
                largestDistance = distance;
                furthestLocation = randLocation;
            }
        }

        // Return the furthest point
        return furthestLocation;
    }

    private static LatLng thetaRandImpl(LatLng originalLocation, int radius, double lowerAngleBound, double upperAngleBound){
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius/ Constants.metersPerDegree;

        // Generate an independent uniform value
        double random1 = random.nextDouble();

        // Calculate a new random radius that is lower than the specified radius
        double newRadius = radiusInDegrees * Math.sqrt(random1);

        // Calculate a new random angle that is between the upper and lower bounds in radians
        double newAngle = random.nextDouble() * (upperAngleBound - lowerAngleBound) + lowerAngleBound;

        // Convert the polar coordinates to cartesian coordinates and return
        return convertPolarToCartesianPoints(originalLocation, newRadius, newAngle);
    }

    private static LatLng convertPolarToCartesianPoints(LatLng originalLocation, double radius, double angle){
        // Find the x and y points given the radius and angle
        double x = radius * Math.cos(angle);
        double y = radius * Math.sin(angle);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double adjustedX = x / Math.cos(Math.toRadians(originalLocation.latitude));

        // Calculate the new point
        double newX = adjustedX + originalLocation.longitude;
        double newY = y + originalLocation.latitude;

        return new LatLng(newY, newX);
    }

    public static boolean eventWithinRange(LatLng originalLocation, LatLng eventLocation, int searchRadius){
        if(distanceBetweenPoints(originalLocation, eventLocation) < searchRadius){
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
