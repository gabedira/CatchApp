package com.example.catchapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.catchapp.databinding.ActivityMapsBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MakeNewRouteActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Button Start;
    Button End;
    private FusedLocationProviderClient mLocationClient;
    private String userName = "Gabriel";
    private Ghost ghost1;
    private Ghost ghost2;
    boolean isPermissionGranted;
    boolean startState;
    MapView mapView;
    //GoogleMap mGoogleMap;


    private double lat;
    private double lng;
    private double oldLat, oldLng;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        //binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(R.layout.make_new_route_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //mapFragment.getMapAsync(this);
        Start = findViewById(R.id.Start);

        checkMyPermission();

        initMap();
        End = findViewById(R.id.End);

        mLocationClient = new FusedLocationProviderClient(this);
        Start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                startState = true;
                Start.setVisibility(View.GONE);
                startTracking();
                End.setVisibility(View.VISIBLE);
            }
        });


        Button Start2 = findViewById(R.id.Start2);
        End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                End.setVisibility(View.GONE);
                Start2.setVisibility(View.VISIBLE);
                endHandler(v);
            }
        });

        Button End2 = findViewById(R.id.End2);
        Start2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Start2.setVisibility(View.GONE);
                End2.setVisibility(View.VISIBLE);
                round2Handler(v);
            }
        });


        End2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("testing", "clicked End2");
                round2EndHandler(v);
                End2.setVisibility(View.GONE);
                //End2Handler(v);
            }
        });





    }


    private int maxTime = 0;
    private TextView tv;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTracking() {
        Log.v("testing", "pos1");
        getCurrLoc();
        ghost1 = new Ghost(userName, LocalDate.now(), LocalTime.now());

//        Drawable drawable = Drawable.createFromPath("ghost.bmp");
//
//        Marker melbourne = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_marker_icon)));


        final Handler handler = new Handler();
        final int delay = 20;
        AtomicReference<Integer> i = new AtomicReference<>();
        i.set(0);

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!startState)
                {
                    ghost1.terminate();
                    maxTime = i.get();

                    TextView tv = (TextView) findViewById(R.id.TotalTime);
                    int minutes = maxTime / 60000;
                    int seconds = (maxTime / 1000) % 60;
                    int decimalPlace = (maxTime % 1000) / 10;
                    double distance = ghost1.getTotalDistance();
                    String out1 = minutes + ":"+seconds+"."+decimalPlace+"        "+String.format("%.2f", distance)+" ft";
                    tv.setText(out1);
                    Toast.makeText(MakeNewRouteActivity.this, "To race against this ghost, proceed to the start location", Toast.LENGTH_LONG).show();
                    return;
                }
                i.set(i.get() + delay);
                oldLat = lat;
                oldLng = lng;
                getCurrLoc();

                tv = (TextView) findViewById(R.id.TotalTime);
                int minutes = i.get() / 60000;
                int seconds = (i.get() / 1000) % 60;
                int decimalPlace = (i.get() % 1000) / 10;
                String out1 = minutes + ":"+seconds+"."+decimalPlace;
                tv.setText(out1);


                ghost1.addPoint(i.get(), new LatLng(lat, lng));
                handler.postDelayed(this, delay);
                Log.v("testing", "time: "+ i + " lat = "+ lat + " lng = "+ lng);

            }
        }, delay);

    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    public void endHandler(View v){
        startState = false;
        Log.v("testing", "set startState to false");




        drawRoute(ghost1);


        //MapsActivity.ghost1s.add(ghost1);



        //End.setVisibility(View.GONE);
        // TODO: Print Distance and elapsed time





        // Ask, do you want to race against this ghost?
        // We click yes

        //round2Handler(v);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void round2EndHandler(View v){
        startState = false;
        Log.v("testing", "set startState to false");
        drawRoute(ghost2);

    }

    private Marker gMarker;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void round2Handler(View v){
        getCurrLoc();
        ghost2 = new Ghost(userName, LocalDate.now(), LocalTime.now());

        for(Polyline line: lines1)
        {
            line.setVisible(false);
        }

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ghost, null);

        gMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) drawable).getBitmap())));

        AtomicReference<Integer> i = new AtomicReference<>();
        i.set(0);

        //if (gMarker != null)
        //    gMarker.remove();
        //gMarker = mMap.addMarker(new MarkerOptions().position(ghost1.route.get(i.get()).getValue()));

        startState = true;

        final Handler handler = new Handler();
        final int delay = 20;
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.ghost_noises);
        mp.setLooping(false);
        handler.postDelayed(new Runnable(){
            public void run(){
                if(!startState)
                {
                    ghost2.terminate();
                    mp.setVolume(0, 0);

                    TextView tv = (TextView) findViewById(R.id.TotalTime);
                    int minutes = i.get() / 60000;
                    int seconds = (i.get() / 1000) % 60;
                    int decimalPlace = (i.get() % 1000) / 10;
                    double distance = ghost2.getTotalDistance();
                    String out1 = minutes + ":"+seconds+"."+decimalPlace+"        "+String.format("%.2f", distance)+" ft";
                    tv.setText(out1);
                    Toast.makeText(MakeNewRouteActivity.this, "To race against this ghost, proceed to the start location", Toast.LENGTH_LONG).show();
                    return;
                }

                tv = (TextView) findViewById(R.id.TotalTime);
                int minutes = i.get() / 60000;
                int seconds = (i.get() / 1000) % 60;
                int decimalPlace = (i.get() % 1000) / 10;
                String out1 = minutes + ":"+seconds+"."+decimalPlace;
                tv.setText(out1);

                i.set(i.get() + delay);
                oldLat = lat;
                oldLng = lng;
                getCurrLoc();
                LatLng g2 = new LatLng(lat, lng);

                int minTime = Math.min(i.get() / delay, ghost1.route.size() - 1);
                double distance = distance(ghost1.route.get(minTime).getValue(), g2);



                minTime = Math.min(minTime, ghost1.route.size() - 1);
                gMarker.setPosition(ghost1.route.get(minTime).getValue());
                //lines1.get(minTime).setVisible(true);

                ghost2.addPoint(i.get(), g2);

                if(distance < 5 && i.get() > 5000)
                {

                    if(!mp.isPlaying()) {
                        mp.setVolume(5, 5);
                        Log.v("testing", "playing noise");
                        mp.start();
                    }
                }
                handler.postDelayed(this, delay);
                Log.v("testing", "time: "+ i + " lat = "+ lat + " lng = "+ lng);

            }
        }, delay);
    }

    double distance(LatLng latlng1, LatLng latlng2)
    {
        double lat1 = latlng1.latitude;
        double lat2 = latlng2.latitude;
        double lon1 = latlng1.longitude;
        double lon2 = latlng2.longitude;

        double R = 6371e3; // metres
        double phi1 = lat1 * Math.PI/180;
        double phi2 = lat2 * Math.PI/180;
        double delphi = (lat2-lat1) * Math.PI/180;
        double delLambda = (lon2-lon1) * Math.PI/180;

        double a = Math.sin(delphi/2) * Math.sin(delphi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(delLambda/2) * Math.sin(delLambda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c * 3.28084; // in feet
    }
    private ArrayList<Polyline> lines1;

    public void drawRoute(Ghost g)
    {
        lines1 = new ArrayList<>();
        for(int i = 1; i < g.route.size(); i++)
        {
            Polyline p = mMap.addPolyline(new PolylineOptions().add(new LatLng(g.route.get(i-1).getValue().latitude,
                            g.route.get(i-1).getValue().longitude)
                    , new LatLng(g.route.get(i).getValue().latitude,
                            g.route.get(i).getValue().longitude)).width(10).color(Color.RED));
            lines1.add(p);
        }
    }
    private void initMap() {
        if(isPermissionGranted)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            //mapFragment.onCreate(savedInstanceState);
        }

    }

    @SuppressLint("MissingPermission")
    private void getCurrLoc() {
            mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Location location = task.getResult();
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    gotoLocation(lat, lng);
                }
            }
        });
    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng LatLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng, 18);
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(mMap.MAP_TYPE_NORMAL);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkMyPermission();
        mMap.setMyLocationEnabled(true);

        getCurrLoc();
    }

    private void checkMyPermission()
    {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse){
                Toast.makeText(MakeNewRouteActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}