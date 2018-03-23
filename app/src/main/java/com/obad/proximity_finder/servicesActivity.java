package com.obad.proximity_finder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Obad on 1/25/18.
 */
public class servicesActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button Trace_Service;
//    EditText Group;
    Spinner GrpLst;
    SharedPreferences app_preferences;

    public static final String TAG = com.obad.proximity_finder.servicesActivity.class.getSimpleName();
    String id;
    String grp;
    public ArrayList<String> grps = new ArrayList<>();

    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1338;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        id = getIntent().getExtras().getString("id");
        // Assigning
        Trace_Service = (Button) findViewById(R.id.buttonTrace);
        GrpLst = (Spinner) findViewById(R.id.grp_lst);
//        Group =(EditText) findViewById(R.id.grp);
        Trace_Service.setOnClickListener(this);
        get_groups();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, grps);
        GrpLst.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }

        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location From GPS
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 500, 0, this);


    }

    public void onLocationChanged ( Location location ){
        ChangeLoc cl = new ChangeLoc();
        cl.execute(location);

    }  // End of onLocationChanged

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    ////######################################################################################################################################################
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    private class ChangeLoc extends AsyncTask<Location, Void, Void> {
        @Override
        protected Void doInBackground(Location... params) {
//            mLatitude  = params[0].getLatitude();
//            mLongitude = params[0].getLongitude( );
//            final LatLng point = new LatLng( mLatitude, mLongitude );
            HttpURLConnection urlConnection = null;

            try {
                String sql = "http://undcemcs02.und.edu/abdelrahman.elsaid/log_loc.php?" +
                        "usr_id="   + id                            +
                        "&lat="     + params[0].getLatitude()       +
                        "&lon="     + params[0].getLongitude();
                URL url = new URL(sql);
                Log.d(TAG, "doInBackground LOGGINH LOCATION: " + sql);
                // Creating an HTTP connection to communicate with URL
                urlConnection = (HttpURLConnection) url.openConnection( );
                // Connecting to URL
//                urlConnection.connect( );
//                urlConnection.getContent();
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return null;
        }

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonTrace:
//                grp = Group.getText().toString();
                grp = GrpLst.getSelectedItem().toString();
                Log.d(TAG, "onClick: " + id +" group: "+ grp);
                Intent i = new Intent(servicesActivity.this, MainActivity.class);
                i.putExtra("id",id);
                i.putExtra("grp",grp);

                startActivity(i);
//                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }


    private void get_groups (){
        ArrayList<String> data = new ArrayList<>();
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL( "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_grps.php?id=" + id);
            // Creating an HTTP connection to communicate with URL
            urlConnection = (HttpURLConnection) url.openConnection( );
            // Connecting to URL
            urlConnection.connect( );
            // Reading data from URL
            iStream = urlConnection.getInputStream( );
            BufferedReader br = new BufferedReader(new InputStreamReader( iStream ) );
            String line = "";
            while( ( line = br.readLine( ) ) != null ) {
                data.add(line);
            }
            br.close( );
        }  // End of try
        catch( Exception e ) {
            Log.d( "DOWNLAOD_DIR_URL", e.toString( ) );
        }
        finally {
            try {
                iStream.close( );
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect( );
        }
        grps = data;
        Log.d(TAG, "doInBackground: I'm INSIDE!!!!!!!!");
        Log.d(TAG, "doInBackground: " + grps);

    }

}

