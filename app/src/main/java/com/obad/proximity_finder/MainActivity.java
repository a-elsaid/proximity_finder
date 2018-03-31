package com.obad.proximity_finder;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Marker;
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



public class MainActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, View.OnClickListener {
    public static final String TAG = DirectionsJSONParser.TAG;
    public String user_id = null;
    public String grp = null;
    public ArrayList<String> grps = null;
    Button back_to_control;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private Integer FORCE_DRAW_USER = null;
    private static final int INITIAL_REQUEST = 1338;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_control:
                Intent i = new Intent(MainActivity.this, servicesActivity.class);
                i.putExtra("id",user_id);
                i.putExtra("grps",grps);
                startActivity(i);
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
    }
    public class user_details {
        public LatLng loc;
        public int avblty;
        public int id;
        public user_details(LatLng latLng, int i, int j) {
            loc     = latLng;
            avblty  = i;
            id  = j;
        }
    }


    GoogleMap mGoogleMap;
    ArrayList<LatLng> mMarkerPoints;
    ArrayList<Marker> mMarkerNames;
    double mLatitude = 0;
    double mLongitude = 0;
    public ArrayList<LatLng> linepoints = new ArrayList<LatLng>();
    public ArrayList<user_details> data = null;

    String value;
    int count_points = 0;
    ToggleButton tbutton;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        back_to_control = (Button) findViewById(R.id.btn_control);
        back_to_control.setOnClickListener(this);
        user_id             = getIntent().getExtras().getString("id");
        grp                 = getIntent().getExtras().getString("grp");
        FORCE_DRAW_USER     = Integer.parseInt(getIntent().getExtras().getString("FORCE_DRAW_USER"));
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
            mMarkerNames = new ArrayList<Marker>();
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
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // TODO Auto-generated method stub
                for (int k=0; k<mMarkerNames.size(); k++) {
                    if (marker.equals(mMarkerNames.get(k))) {
                        Log.w("Click", "test");
                        Intent i = new Intent(MainActivity.this, requestActivity.class);
                        i.putExtra("user_id",user_id);
                        Log.d(TAG, "onMarkerClick: " + data.get(k).id);
                        i.putExtra("requestee_id", String.valueOf(data.get(k).id));
                        startActivity(i);
                        finish();
                        return true;
                    }

                }
                return false;
            }
        });
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
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
        final Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 500, 0, this);
//        final Handler handler =new Handler();
//        final Runnable r = new Runnable() {
//            public void run() {
//                handler.postDelayed(this, 30000);
//                ChangeLoc cl = new ChangeLoc();
//                cl.execute(location);
//            }
//        };
//        handler.postDelayed(r, 0000);

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


    // A method to download users locations
    private ArrayList<user_details> downloadUsersLocs( LatLng user_location ) throws IOException {
        data   = new ArrayList<user_details>();
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_users_locs.php?user_location=" +
                user_location.latitude + ',' +
                user_location.longitude +
                "&usr_id=" + user_id +
                "&grp=" + grp;
        Log.d(TAG, "doInBackground: GET USERS LOCATIONS:: " + str);
        try {
            URL url = new URL( str);
            urlConnection = (HttpURLConnection) url.openConnection( );
            urlConnection.connect( );
            iStream = urlConnection.getInputStream( );
            BufferedReader br = new BufferedReader(
                    new InputStreamReader( iStream ) );
            String line = "";
            while( ( line = br.readLine( ) ) != null ) {
                String[] userDetails    =  line.split(",");
                double latitude     = Double.parseDouble(userDetails[0]);
                double longitude    = Double.parseDouble(userDetails[1]);
                int    availability = Integer.parseInt(userDetails[2]);
                int    other_id = Integer.parseInt(userDetails[3]);
                LatLng loc = new LatLng(latitude, longitude);
//                Log.d(TAG, "doInBackground: GET USERS LOCATIONS:: " + str);
                Log.d(TAG, "doInBackground: FETCHED POINT:: " + (loc.toString()));
                data.add(new user_details(loc, availability, other_id));

                Log.d(TAG, "downloadUsersLocs: DATA LIST CONTENT: " +  (data.size()-1) + " " + data.get(data.size()-1).loc.toString());
            }
            br.close( );
        }
        catch( Exception e ) {
            Log.d( "DOWNLAOD_DIR_URL", e.toString( ) );
        }
        finally {
            iStream.close( );
            urlConnection.disconnect( );
        }


        return data;
    }  // End of downloadUsersLocs


    // A method to download JSON data from URL
    private String downloadJSON( LatLng user_location, LatLng other_user_location ) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            String str = "https://maps.googleapis.com/maps/api/directions/json?origin=" + user_location.latitude + "," +user_location.longitude+ "&destination=" + other_user_location.latitude + "," + other_user_location.longitude + "&key=AIzaSyAlkU9AVksyIyxrTRRjGRSSfN1uEOFSoeo";
            Log.d(TAG, "downloadJSON - DOWNLAOD_DIR_URL: URL: " + str);
            SystemClock.sleep(2000);
            URL url = new URL( str);

            urlConnection = (HttpURLConnection) url.openConnection( );
            urlConnection.connect( );
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
    }  // End of downloadJSON

    private void drawMarker( ArrayList<user_details> users ) {
        mMarkerPoints.clear();
        mGoogleMap.clear();
        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions( );
        for(user_details userDetails : users){
            if (userDetails.avblty==3){
                mMarkerPoints.add( userDetails.loc );
                // Setting the position of the marker
                options.position( userDetails.loc );
                options.icon( BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE));
                options.title("User: " + userDetails.id);

            }
            else if (userDetails.avblty==0){
                mMarkerPoints.add( userDetails.loc );
                // Setting the position of the marker
                options.position( userDetails.loc );
                options.icon( BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN));
                options.title("User: " + userDetails.id);

            }
            else {
                mMarkerPoints.add( userDetails.loc );
                // Setting the position of the marker
                options.position( userDetails.loc );
                options.icon( BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED));
                options.title("User: " + userDetails.id);
//                options.icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)));
            }
            mMarkerNames.add( mGoogleMap.addMarker(options));
            mMarkerNames.get(mMarkerNames.size()-1).showInfoWindow();
//            boolean click = onMarkerClick(mMarkerNames.get(mMarkerNames.size()-1));
//            Log.d(TAG, "drawMarker: " + click);
        }
//        runOnUiThread(new Runnable() {
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
            Log.d(TAG, "doInBackground: I AM INSIDE CHANGELOC!");
            mLatitude  = params[0].getLatitude();
            mLongitude = params[0].getLongitude( );
            final LatLng point = new LatLng( mLatitude, mLongitude );
            logloc(point);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                }
            });

            ArrayList<user_details> data = new ArrayList();
            try {
                // Fetching the data from web service
                Log.d(TAG, "doInBackground: I WILL START ASKING FOR THE USERS LOCATIONS AND THEIR NEAREST!");
                data = downloadUsersLocs( point );
            }
            catch( Exception e ) {
                Log.e( "Background Task", e.toString( ) );
            }

            data.add(new user_details(new LatLng(mLatitude, mLongitude), 3, Integer.parseInt(user_id)));

            // Draw the marker, if destination location is not set.
            final ArrayList<user_details> finalData = data;
            runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawMarker(finalData);
                    }
                });

            LatLng other_user_point = null;
            for (user_details other_user : data) {
                if(FORCE_DRAW_USER!=0){
                    if (other_user.id==FORCE_DRAW_USER){
                        other_user_point = new LatLng(other_user.loc.latitude, other_user.loc.longitude);
                        break;
                    }
                }
                else{
                    if (other_user.avblty==0){
                        other_user_point = new LatLng(other_user.loc.latitude, other_user.loc.longitude);
                        break;
                    }
                }

            }
//            LatLng other_user_point = new LatLng(data.get(0).loc.latitude, data.get(0).loc.longitude);

            String results = "";
            try {
                Log.d(TAG, "doInBackground: POINT1: " + point.toString() + " POINT2: " + other_user_point.toString());
                results = downloadJSON(point, other_user_point);
            } catch (IOException e) {
                Log.e("doInBackground: ERROR IN DOWNLOADJSON ", e.toString());
                e.printStackTrace();
            }
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

    private void logloc (LatLng loc){
        HttpURLConnection urlConnection = null;
        try {
            String sql = "http://undcemcs02.und.edu/abdelrahman.elsaid/log_loc.php?" +
                    "usr_id="   + user_id            +
                    "&lat="     + loc.latitude       +
                    "&lon="     + loc.longitude;
            URL url = new URL(sql);
            Log.d(TAG, "logloc: " + sql);
            urlConnection = (HttpURLConnection) url.openConnection( );
            urlConnection.connect( );
            urlConnection.getContent();
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        finally {
            urlConnection.disconnect( );
        }
    }

}  // End of MainActivity
