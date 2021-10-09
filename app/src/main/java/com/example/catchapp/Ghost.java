package com.example.catchapp;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.model.LatLng;

import java.util.AbstractMap;
import java.util.ArrayList;


public class Ghost {

    Ghost(LatLng currLoc){
        route = new ArrayList<>();
        route.add(new AbstractMap.SimpleEntry<>(0, currLoc));
    }

    public void addPoint(int time, LatLng latLng)
    {
        route.add(new AbstractMap.SimpleEntry<>(time, latLng));
    }

    private ArrayList<AbstractMap.SimpleEntry<Integer, LatLng>> route;
}
