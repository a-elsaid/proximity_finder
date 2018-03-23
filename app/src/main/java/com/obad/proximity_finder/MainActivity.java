package com.obad.proximity_finder;


import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {
    public static final String TAG = DirectionsJSONParser.TAG;
    public String user_id = null;
    public String grp = null;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1338;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 1;


    GoogleMap mGoogleMap;
    ArrayList<LatLng> mMarkerPoints;
    double mLatitude = 0;
    double mLongitude = 0;
    public ArrayList<LatLng> linepoints = new ArrayList<LatLng>();

    String value;
    int count_points = 0;
    ToggleButton tbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_id = getIntent().getExtras().getString("id");
        grp     = getIntent().getExtras().getString("grp");
        Log.d(TAG, "onCreate MainActivity id: " + user_id + " grp: " +grp);
//        value = getIntent().getExtras().getString("email");
//        value = "TEST@EMAIL.COM";
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(
                getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available.
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    status, this, requestCode);
            dialog.show();
        } else {
            // Google Play Services are available.
            // Initializing
            mMarkerPoints = new ArrayList<LatLng>();
            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment fm = (SupportMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting Map for the SupportMapFragment
            fm.getMapAsync(this);

        }  // End of else
    }  // End of onCreate



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }
        // Enables MyLocation Button in the Map.
        mGoogleMap.setMyLocationEnabled(true);

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

    ////######################################################################################################################################################
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }
////######################################################################################################################################################




    public void drawline (ArrayList<LatLng> points){
        PolylineOptions lineOptions = null;
        lineOptions = new PolylineOptions( );
        // Adding all the points in the route to LineOptions
        lineOptions.addAll(points);
        lineOptions.width(2);
        lineOptions.color(Color.RED);
        // Drawing polyline between points
        mGoogleMap.addPolyline(lineOptions);


    }


    // A method to download JSON data from URL
    private ArrayList<LatLng> downloadUrl( LatLng user_location ) throws IOException {
        ArrayList<LatLng> data = new ArrayList<>();
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL( "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_direction.php?user_location=" +
                    user_location.latitude + ',' +
                    user_location.longitude +
                    "&usr_id=" + user_id +
                    "&grp=" + grp);
            Log.d(TAG, "downloadUrl URL:: " + url);
            // Creating an HTTP connection to communicate with URL
            urlConnection = (HttpURLConnection) url.openConnection( );
            // Connecting to URL
            urlConnection.connect( );
            // Reading data from URL
            iStream = urlConnection.getInputStream( );
            BufferedReader br = new BufferedReader(
                    new InputStreamReader( iStream ) );
            String line = "";
            while( ( line = br.readLine( ) ) != null ) {
                String[] latlong =  line.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude= Double.parseDouble(latlong[1]);
                LatLng loc = new LatLng(latitude, longitude);
                data.add(loc);
            }
            br.close( );
        }  // End of try
        catch( Exception e ) {
            Log.d( "DOWNLAOD_DIR_URL", e.toString( ) );
        }

//        try {
//            URL url = new URL("http://undcemcs02.und.edu/abdelrahman.elsaid/log_loc.php?" +
//                    "usr_id="   + user_id                      +
//                    "&lat="     + user_location.latitude       +
//                    "&lon="     + user_location.longitude      );
//        }
        finally {
            iStream.close( );
            urlConnection.disconnect( );
        }


        return data;
    }  // End of downloadUrl


    // A method to download JSON data from URL
    private String downloadJSON( LatLng user_location, LatLng other_user_location ) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL( "https://maps.googleapis.com/maps/api/directions/json?origin=" + user_location.latitude + "," +user_location.longitude+ "&destination=" + other_user_location.latitude + "," + other_user_location.longitude + "&key=AIzaSyAlkU9AVksyIyxrTRRjGRSSfN1uEOFSoeo");
            Log.d(TAG, "downloadUrl URL:: " + url);
            // Creating an HTTP connection to communicate with URL
            urlConnection = (HttpURLConnection) url.openConnection( );
            // Connecting to URL
            urlConnection.connect( );
            // Reading data from URL
            iStream = urlConnection.getInputStream( );
            BufferedReader br = new BufferedReader(
                    new InputStreamReader( iStream ) );
            StringBuffer sb  = new StringBuffer( );
            String line = "";
            while( ( line = br.readLine( ) ) != null ) {
                sb.append( line );
            }
            data = sb.toString( );
            br.close( );
        }  // End of try
        catch( Exception e ) {
            Log.d( "DOWNLAOD_DIR_URL", e.toString( ) );
        }
        finally {
            iStream.close( );
            urlConnection.disconnect( );
        }
        return data;
    }  // End of downloadUrl

    private void drawMarker( ArrayList<LatLng> points ) {
        mMarkerPoints.clear();
        mGoogleMap.clear();
        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions( );
        int i =0;
        for(LatLng point : points){
            Log.d(TAG, "drawMarker: " + i + ":: "+ point);
            if (i==points.size()-1){
                mMarkerPoints.add( point );
                // Setting the position of the marker
                options.position( point );
                options.icon( BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN));
            }
            else {
                mMarkerPoints.add(point);
                // Setting the position of the marker
                options.position(point);
                options.icon(BitmapDescriptorFactory.defaultMarker(
                        new Random().nextInt(360)));
            }
            mGoogleMap.addMarker(options);
            i++;
        }
//        runOnUiThread(new Runnable() {

            Log.d(TAG, "run mMarkerPoints: " +mMarkerPoints);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : mMarkerPoints) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));

    }  // End of drawMarker

    private class ChangeLoc extends AsyncTask <Location, Void, String>{
        @Override
        protected String doInBackground(Location... params) {
            Log.d("InBackGround Current User Location", params[0].toString());
            mLatitude  = params[0].getLatitude();
            mLongitude = params[0].getLongitude( );
            final LatLng point = new LatLng( mLatitude, mLongitude );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                }
            });
            ArrayList<LatLng> data = new ArrayList();
            try {
                // Fetching the data from web service
                data = downloadUrl( point );
                Log.d(TAG, "doInBackground return of the server: ");
            }
            catch( Exception e ) {
                Log.d( "Background Task", e.toString( ) );
            }

            data.add(new LatLng(mLatitude, mLongitude));

            // Draw the marker, if destination location is not set.
            final ArrayList<LatLng> finalData = data;
            Log.d(TAG, "finalData: "+finalData);
            runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: I'm trying to draw the marker!!");
                        drawMarker(finalData);
                    }
                });

            LatLng other_user_point = new LatLng(data.get(0).latitude, data.get(0).longitude);
            String results = "";
            try {
                results = downloadJSON(point, other_user_point);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground JASON DATA: " + results);
            return results;

        }
        // Executes in UI thread, after the execution of doInBackground( )
        @Override
        protected void onPostExecute( String result ) {
            super.onPostExecute( result );
            ParserTask parserTask = new ParserTask( );
            // Invokes the thread for parsing the JSON data.
            parserTask.execute( result );
        }  // End of onPostExecute
    }

    // A class to parse the Google Directions in JSON format
    private class ParserTask extends AsyncTask<String, Integer,
            List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>>
        doInBackground( String... jsonData ) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject( jsonData[0] );
                DirectionsJSONParser parser = new DirectionsJSONParser( );
                // Starts parsing data.
                routes = parser.parse( jObject );
            }
            catch( Exception e ) {
                e.printStackTrace( );
            }
            return routes;
        }  // End of doInBackground


        // Executes in UI thread, after the parsing process.
        @Override
        protected void onPostExecute(
                List<List<HashMap<String, String>>> result ) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for ( int i=0; i<result.size( ); i++ ) {
                points = new ArrayList<LatLng>( );
                lineOptions = new PolylineOptions( );
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get( i );

                // Fetching all the points in i-th route
                for ( int j=0; j<path.size( ); j++ ) {
                    HashMap<String,String> point = path.get( j );
                    double lat = Double.parseDouble( point.get( "lat" ) );
                    double lng = Double.parseDouble( point.get( "lng" ) );
                    LatLng position = new LatLng( lat, lng );
                    points.add( position );
                }  // End of inner for

                // Adding all the points in the route to LineOptions
                lineOptions.addAll( points );
                lineOptions.width( 5 );
                lineOptions.color( Color.RED );
            }  // End of outer for

            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline( lineOptions );
        }  // End of onPostExecute

    }  // End of ParserTask


    @Override
    public void onLocationChanged ( Location location ){
        ChangeLoc cl = new ChangeLoc();
        cl.execute(location);

    }  // End of onLocationChanged

    @Override
    public void onProviderDisabled( String provider ) {
        // TODO Auto-generated method stub
        Log.d("onProviderDisabled", "onProviderDisabled: Check LocationProvier (Disables)");
    }  //  End of onProviderDisabled

    @Override
    public void onProviderEnabled( String provider ) {
        // TODO Auto-generated method stub
    }  // End of onProviderEnabled

    @Override
    public void onStatusChanged(
            String provider, int status, Bundle extras ) {
        // TODO Auto-generated method stub
    }  // End of onStatusChanged

}  // End of MainActivity
