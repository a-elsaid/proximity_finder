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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.CYAN;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.RED;

/**
 * Created by Obad on 1/25/18.
 */
public class servicesActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    Button Trace_Service;
    Button LogOut;
    LinearLayout inSCRL;
    TextView outTV;
    ToggleButton Free_Busy;
//    EditText Group;
    Spinner GrpLst;
    SharedPreferences app_preferences;

    public static final String TAG = com.obad.proximity_finder.servicesActivity.class.getSimpleName();
    String id;
    String grp;
    Integer status;
    public ArrayList<String> grps = new ArrayList<>();
    private String requested_user_status = null;
    private String requested_user_id     = null;

    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1338;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 1;
    public ArrayList<String> requests_recs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        inSCRL  = (LinearLayout) findViewById(R.id.inScroll_lo);
        outTV   = (TextView) findViewById(R.id.tv_outreq);
        outTV.setVisibility(View.INVISIBLE);

        final Handler handler =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 30000);
                try {
                    GetRequests gr = new GetRequests();
                    requests_recs = gr.execute().get();
                    inSCRL.removeAllViews();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                for(String rec: requests_recs){
                    String[] requestDetails    =  rec.split(",");
                    if(requestDetails[0].equals(id)){
                        Toast.makeText(servicesActivity.this, "I request" + requestDetails[1], Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: I REQUEST: " + requestDetails[1]);
                        requested_user_id    = requestDetails[1];
                        requested_user_status =requestDetails[2];
                        Log.d(TAG, "run: MY REQUEST STATUS: " + requestDetails[2]);
                        if (requested_user_status.equals("2")) {
                            outTV.setText("User: " + requestDetails[1] + " ACCEPTED... TAB TO GET DIRECTION");
                            outTV.setTextColor(GREEN);
                            outTV.setOnClickListener(servicesActivity.this);
                            outTV.setVisibility(View.VISIBLE);
                        }
                        else if (requested_user_status.equals("3")) {
                            outTV.setText("User: " + requestDetails[1] + " REJECTED... TAB TO DISMISS");
                            outTV.setTextColor(RED);
                            outTV.setOnClickListener(servicesActivity.this);
                            outTV.setVisibility(View.VISIBLE);
                        }
                        else if  (requested_user_status.equals("1")) {
                            outTV.setTextColor(CYAN);
                            outTV.setText("User: " + requestDetails[1] + " PENDING");
                            outTV.setVisibility(View.VISIBLE);
                        }
                        else if (requested_user_status.equals("0")) {
                            outTV.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        Log.d(TAG, "run: THE ID IS: " + id);
                        Log.d(TAG, "run: THE ID vs requestDetails IS: " + (requestDetails[0]==id));
                        Log.d(TAG, "run: I AM REQUESTED BY: " + requestDetails[0]);
                        LinearLayout inScrolRow = (LinearLayout) new LinearLayout(servicesActivity.this);
                        TextView tv = new TextView(servicesActivity.this);
                        tv.setText("Request From User: " + requestDetails[0]);
                        tv.setTextSize(18);
                        tv.setTextColor(BLUE);
                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        Button accpt = new Button(servicesActivity.this);
                        accpt.setOnClickListener(servicesActivity.this);
                        accpt.setText("ACCEPT");
                        accpt.setId(Integer.parseInt("13" + requestDetails[0]));
                        Button deny = new Button(servicesActivity.this);
                        deny.setOnClickListener(servicesActivity.this);
                        deny.setText("DISMISS");
                        deny.setId(Integer.parseInt("14" + requestDetails[0]));
                        inScrolRow.addView(accpt);
                        inScrolRow.addView(deny);
                        inScrolRow.addView(tv);
                        inSCRL.addView(inScrolRow);
                    }
                }
//                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
//                edittextId.setText(mydate);
            }
        };
        handler.postDelayed(r, 0000);
        id = getIntent().getExtras().getString("id");

//        get_groups();
        Get_groups G = new Get_groups();
        try {
            grps = G.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//        get_status();
        Get_Stauts gs = new Get_Stauts();
        try {
            status = gs.execute(id).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate: THE STATUS VALUE AFTER ASYNC::" +status);
        Log.d(TAG, "onCreate: THE grps VALUE AFTER ASYNC::" +grps.toString());
        Trace_Service = (Button) findViewById(R.id.buttonTrace);
        Free_Busy = (ToggleButton) findViewById(R.id.buttonfree_busy);
        if (status==0) {
            Free_Busy.setText("FREE");
            Free_Busy.setBackgroundColor(GREEN);
        }
        else {
            Free_Busy.setText("BUSY");
            Free_Busy.setBackgroundColor(MAGENTA);
        }

        Free_Busy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Change_status cst = new Change_status();
                if (isChecked) {
                    Free_Busy.setTextOn("BUSY");
                    Free_Busy.setBackgroundColor(MAGENTA);
                    status = 1;

                } else {
                    Free_Busy.setTextOff("FREE");
                    Free_Busy.setBackgroundColor(GREEN);
                    status = 0;
                }
                cst.execute(status);
            }
        });
        LogOut = (Button) findViewById(R.id.btn_logout);
        GrpLst = (Spinner) findViewById(R.id.grp_lst);
        Trace_Service.setOnClickListener(this);
        Free_Busy.setOnClickListener(this);
        LogOut.setOnClickListener(this);

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
            HttpURLConnection urlConnection = null;
            try {
                String str = "http://undcemcs02.und.edu/abdelrahman.elsaid/log_loc.php?" +
                        "usr_id="   + id                            +
                        "&lat="     + params[0].getLatitude()       +
                        "&lon="     + params[0].getLongitude();
                URL url = new URL(str);
                Log.d(TAG, "doInBackground LOGGING LOCATION: " + str);
                urlConnection = (HttpURLConnection) url.openConnection( );
                urlConnection.connect( );
                urlConnection.getContent();

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            finally {
                urlConnection.disconnect( );
            }
            return null;
        }
    }



    @Override
    public void onClick(View v) {
        int VIEW = v.getId();
        if (VIEW == R.id.buttonTrace){
            grp = GrpLst.getSelectedItem().toString();
            Log.d(TAG, "onClick: " + id +" group: "+ grp);
            Intent i = new Intent(servicesActivity.this, MainActivity.class);
            i.putExtra("id",id);
            i.putExtra("grp",grp);
            i.putExtra("FORCE_DRAW_USER",0);
            startActivity(i);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else if (VIEW==R.id.btn_logout){
            Log.d(TAG, "onClick: " + id +" group: "+ grp);
            startActivity(new Intent(this, accountActivity.class));
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else if(VIEW==R.id.tv_outreq){
            UpdateRequests ur = new UpdateRequests();
            String[] strarr = new String[]{id, "0"};
                                ur.execute(strarr);
            if(requested_user_status.equals("2")) {
                Toast.makeText(this, "ACCEPTED!!!!!!!!!", Toast.LENGTH_SHORT).show();
                outTV.setVisibility(View.INVISIBLE);
                grp = GrpLst.getSelectedItem().toString();
                Log.d(TAG, "onClick: " + id +" group: "+ grp);
                Intent i = new Intent(servicesActivity.this, MainActivity.class);
                i.putExtra("id",id);
                i.putExtra("grp",grp);
                i.putExtra("FORCE_DRAW_USER", requested_user_id);
                startActivity(i);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            else if (requested_user_status.equals("3")){
                Toast.makeText(this, "REJECTED!!!!!!!!!", Toast.LENGTH_SHORT).show();
                outTV.setVisibility(View.INVISIBLE);
            }
        }
        else {
            for (String requestee: requests_recs){
                String[] xx = requestee.split(",");
                if (!xx[0].equals(id.toString())) {
                    if (VIEW == Integer.parseInt("13".concat(xx[0]))) {
                        UpdateRequests ur = new UpdateRequests();
//                        String[] strarr = new String[]{requestee, "2"};
                        String[] strarr = new String[]{xx[0], "2"};
                        Toast.makeText(this, "Accepting Rquest From User: " + xx[0], Toast.LENGTH_SHORT).show();
                        ur.execute(strarr);
                        ViewGroup parent = (ViewGroup) v.getParent();
                        inSCRL.removeView(parent);


//                        ((ViewGroup) v.getParent()).removeViews(View.findViewById(Integer.parseInt("14".concat(xx[0]))));
                    } else if (VIEW == Integer.parseInt("14".concat(xx[0]))) {
                        UpdateRequests ur = new UpdateRequests();
                        String[] strarr = new String[]{xx[0], "3"};
                        Toast.makeText(this, "Rejecting Rquest From User: " + xx[0], Toast.LENGTH_SHORT).show();
                        ur.execute(strarr);
                        ViewGroup parent = (ViewGroup) v.getParent();
                        inSCRL.removeView(parent);

                    }
                }
            }
        }
    }


    private class Get_Stauts extends AsyncTask<String, Void, Integer>{
        Integer s = null;
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        @Override
        protected Integer doInBackground(String... id) {
            try {
                String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_avail.php?id=" + id[0];
                Log.d(TAG, "get_status: " + str);
                URL url = new URL(str);
                urlConnection = (HttpURLConnection) url.openConnection( );
                urlConnection.connect( );
                iStream = urlConnection.getInputStream( );
                BufferedReader br = new BufferedReader(new InputStreamReader( iStream ) );
                s = Character.getNumericValue(br.read());
                br.close();
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
            return s;
        }

        protected Integer[] onPostExecute(Integer... st){
            return st;
        }
    }
    private Integer get_status (){
        Integer st = null;
        ArrayList<String> data = new ArrayList<>();
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_avail.php?id=" + id;
            Log.d(TAG, "get_status: " + str);
            URL url = new URL(str);
            urlConnection = (HttpURLConnection) url.openConnection( );
            urlConnection.connect( );
            iStream = urlConnection.getInputStream( );
            BufferedReader br = new BufferedReader(new InputStreamReader( iStream ) );
            status = Character.getNumericValue(br.read());
            br.close();
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
        return st;
    }

        private class Get_groups extends AsyncTask <Location, Void, ArrayList<String>>{
        ArrayList<String> data = new ArrayList<>();
        InputStream iStream_ = null;
        HttpURLConnection urlConnection_ = null;
            @Override
            protected ArrayList<String> doInBackground(Location... locations) {
                try {
                    String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_grps.php?id=" + id;
                    URL url_ = new URL(str);
                    Log.d(TAG, "get_groups: " + str);
                    urlConnection_ = (HttpURLConnection) url_.openConnection( );
                    Log.d(TAG, "get_groups: HERE FROM GET GROUPS AFTER ISTREAM");
                    urlConnection_.connect( );

                    iStream_ = urlConnection_.getInputStream( );
                    BufferedReader br = new BufferedReader(new InputStreamReader( iStream_ ) );
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
                        iStream_.close( );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    urlConnection_.disconnect( );
                }
                return data;
            }
            protected ArrayList<String>[] onPostExecute(ArrayList<String>... g){
                return g;
            }
    }


    private class Change_status extends AsyncTask <Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... status) {
            ArrayList<String> data = new ArrayList<>();
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/chng_status.php?usr_id=" + id + "&usr_status=" + status[0];
                URL url = new URL(str);
                Log.d(TAG, "get_groups: " + str);
                urlConnection = (HttpURLConnection) url.openConnection( );
                urlConnection.connect( );
                urlConnection.getContent();
            }  // End of try
            catch( Exception e ) {
                Log.d( "DOWNLAOD_DIR_URL", e.toString( ) );
            }
            finally {
                urlConnection.disconnect( );
            }
            return null;
        }
    }
    private class GetRequests extends AsyncTask <Integer, Void, ArrayList<String>> {
        ArrayList<String> data = new ArrayList<String>();
        InputStream iStream_ = null;
        HttpURLConnection urlConnection_ = null;
        @Override
        protected ArrayList<String> doInBackground(Integer... i) {
            try {
                String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/get_requests.php?id=" + id;
                URL url_ = new URL(str);
                Log.d(TAG, "get_groups: " + str);
                urlConnection_ = (HttpURLConnection) url_.openConnection();
                Log.d(TAG, "get_groups: HERE FROM GET GROUPS AFTER ISTREAM");
                urlConnection_.connect();

                iStream_ = urlConnection_.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream_));
                String line = "";
                while ((line = br.readLine()) != null) {
                    data.add(line);
                }
                br.close();
            }  // End of try
            catch (Exception e) {
                Log.d("DOWNLAOD_DIR_URL", e.toString());
            } finally {
                try {
                    iStream_.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlConnection_.disconnect();
            }
            return data;
        }

        protected ArrayList<String>[] onPostExecute(ArrayList<String>... r) {
            return r;
        }
    }

    private class UpdateRequests extends AsyncTask<String, Void, Void> {
        HttpURLConnection urlConnection_ = null;
        @Override
        protected Void doInBackground(String... req) {
            String str = "http://undcemcs02.und.edu/~abdelrahman.elsaid/update_requests.php?id=" + req[0] + "&reqstat=" + req[1];
            try {
                Log.d(TAG, "doInBackground: STRING OF UPDATE RESQUEST TABLE: " + str);
                URL url_ = new URL(str);
                urlConnection_ = (HttpURLConnection) url_.openConnection();
//                urlConnection_.connect();
            }  // End of try
            catch (Exception e) {
                Log.d("DOWNLAOD_DIR_URL", e.toString());
            } finally {
                urlConnection_.disconnect();
            }
            return null;
        }
    }
}


