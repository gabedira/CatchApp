package com.example.catchapp;

import android.graphics.Color;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;


public class Ghost {
    private String userName;
    private LocalDate dateMade;
    private LocalTime timeMade;
    public ArrayList<AbstractMap.SimpleEntry<Integer, LatLng>> route;
    private double totalDistance;
    private int totalTime;
    private boolean mutable;

    Ghost(String user, LocalDate date, LocalTime time){
        userName = user;
        dateMade = date;
        timeMade = time;
        route = new ArrayList<>();
        mutable = true;
    }

    public void addPoint(int time, LatLng latLng)
    {
        if(mutable)
            route.add(new AbstractMap.SimpleEntry<>(time, latLng));
    }

    public void terminate()
    {
        mutable = false;
        totalTime = route.get(route.size()-1).getKey();
        totalDistance = 0;
        for(int i = 1; i < route.size(); i++)
        {
            double lat1 = route.get(i-1).getValue().latitude;
            double lat2 = route.get(i).getValue().latitude;
            double lon1 = route.get(i-1).getValue().longitude;
            double lon2 = route.get(i).getValue().longitude;

            double R = 6371e3; // metres
            double phi1 = lat1 * Math.PI/180;
            double phi2 = lat2 * Math.PI/180;
            double delphi = (lat2-lat1) * Math.PI/180;
            double delLambda = (lon2-lon1) * Math.PI/180;

            double a = Math.sin(delphi/2) * Math.sin(delphi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(delLambda/2) * Math.sin(delLambda/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            totalDistance += R * c * 3.28084; // in feet
        }
        // calculates distance and time
    }

    public String getName()
    {
        return userName;
    }

    public LocalDate getDate()
    {
        return dateMade;
    }

    public LocalTime getTime()
    {
        return timeMade;
    }

    public int getTotalTime()
    {
        return totalTime;
    }

    public double getTotalDistance()
    {
        return totalDistance;
    }
}
