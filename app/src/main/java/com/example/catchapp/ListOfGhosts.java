package com.example.catchapp;

import android.Manifest;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ListOfGhosts extends ListActivity {
    private ListView lv;
    public List<Ghost> ghosts;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.v("testing", "in list activity");
        setContentView(R.layout.ghosts_lists);

        lv = (ListView) findViewById(R.id.list2);




        /*ArrayList<String> ghostNames = new ArrayList<>();
        for(int i = 0; i< MapsActivity.ghosts.size(); i++)
        {
            ghostNames.add(MapsActivity.ghosts.get(i).getName());
            Log.v("testing", ""+ ghostNames.get(i));
        }
        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                ghostNames);

        lv.setAdapter(arrayAdapter);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initGhosts() {
        ghosts = new ArrayList<>();
        if(ActivityCompat.
                checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            File file = new File(Environment.getExternalStorageDirectory()+"/CatchAppRecords");

            try (FileInputStream inputStream = new FileInputStream(file)) {
                while (inputStream.available() > 0) {
                    try (ObjectInputStream input = new ObjectInputStream(inputStream)) {
                        Ghost ghost = (Ghost) input.readObject();
                        ghosts.add(ghost);
                    } catch (ClassNotFoundException e) {
                        Log.v("testing", "Cannot deserialize a ghost");
                        ghosts.clear();
                    }
                }
            } catch (FileNotFoundException e) {
                Log.v("testing", "File with ghosts not found, trying to create it");
                try {
                    Files.createDirectories(file.toPath());
                } catch (IOException ex) {
                    Log.v("testing", "Unable to create a file");
                }
            } catch (IOException e) {
                Log.v("testing", "Cannot obtain ghosts from file");
                ghosts.clear();
            }
        }
    }
}

