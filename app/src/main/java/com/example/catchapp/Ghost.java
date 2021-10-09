package com.example.catchapp;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.ArrayList;


public class Ghost {
    private String userName;
    private LocalDate dateMade;
    private LocalTime timeMade;
    private ArrayList<AbstractMap.SimpleEntry<Integer, LatLng>> route;
    private double totalDistance;
    private int totalTime;
    private boolean mutable;

    Ghost(String user, LocalDate date, LocalTime time, LatLng currLoc){
        userName = user;
        dateMade = date;
        timeMade = time;
        route = new ArrayList<>();
        route.add(new AbstractMap.SimpleEntry<>(0, currLoc));
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
