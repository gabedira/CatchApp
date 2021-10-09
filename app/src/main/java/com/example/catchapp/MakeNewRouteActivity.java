package com.example.catchapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.StandardOpenOption;
import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class MakeNewRouteActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Button Start;
    Button End;
    private FusedLocationProviderClient mLocationClient;
    private String userName = "Gabriel";
    private Ghost ghost;
    boolean isPermissionGranted;
    boolean startState;
    MapView mapView;
    //GoogleMap mGoogleMap;


    private double lat;
    private double lng;

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

        mLocationClient = new FusedLocationProviderClient(this);
        Start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                startState = true;
                Start.setVisibility(View.GONE);
                startTracking();
            }
        });




    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTracking() {
        Log.v("testing", "pos1");
        getCurrLoc();
        ghost = new Ghost(userName, LocalDate.now(), LocalTime.now(), new LatLng(lat, lng));

        final Handler handler = new Handler();
        final int delay = 100;
        AtomicReference<Integer> i = new AtomicReference<>();
        i.set(0);

        handler.postDelayed(new Runnable(){
            public void run(){
                if(!startState)
                {
                    ghost.terminate();
                    return;
                }
                i.set(i.get() + delay);
                getCurrLoc();
                ghost.addPoint(i.get(), new LatLng(lat, lng));
                handler.postDelayed(this, delay);
                Log.v("testing", "time: "+ i + " lat = "+ lat + " lng = "+ lng);
            }
        }, delay);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void endHandler(View v){
        startState = false;
        Log.v("testing", "set startState to false");
        //MapsActivity.ghosts.add(ghost);

        //FileInputStream in = null;
        FileOutputStream fos = null;
        Date date = new Date();

        try{
            String fileName = "CatchAppRecords/"+String.valueOf(System.currentTimeMillis());
            File myFile = new File(fileName);
            myFile.createNewFile();

            fos = new FileOutputStream(fileName);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(ghost);
            os.close();

        } catch (Exception ex) {
            Log.v("testing", "error pos 1");
            ex.printStackTrace();
        }


        //End.setVisibility(View.GONE);
        // TODO: Shows you stats. For now it will take you back to home page
        Intent switchActivityIntent = new Intent(this, MapsActivity.class);
        startActivity(switchActivityIntent);
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
                gotoLocation(location.getLatitude(), location.getLongitude());
                lat = location.getLatitude();
                lng = location.getLongitude();
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