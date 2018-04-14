package hr.fer.collectors;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


import hr.fer.connection.HTTPURLConnection;

public class LocationService
        extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private HTTPURLConnection service;
    private int success = 0;
    private String path = "https://zavradmb2018.000webhostapp.com/addlocation.php";

    private int userid = 0;
    private double latitude = 0;
    private double longitude = 0;

    private Date datetime = new Date();

    public static final long NOTIFY_INTERVAL = 5 * 1000; // 10 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    private LocationManager locationManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);

        service = new HTTPURLConnection();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }


    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // display toast
                    Toast.makeText(getApplicationContext(), getLocation(),
                            Toast.LENGTH_SHORT).show();

                    new PostDataTOServer().execute();
                }

            });
        }

        private String getLocation() {
            Location lastLocation;
            try {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            } catch (SecurityException ex) {
                return "Error occured.";
            }
            latitude = lastLocation.getLatitude();
            longitude= lastLocation.getLongitude();
            datetime = new Date();

            String result = "Latitude: " + latitude + " Longitude: " + longitude
                    + " " + new Date().toString();

            return result;
        }

    }

    private class PostDataTOServer extends AsyncTask<Void, Void, Void> {
        String response = "";
        //Create hashmap Object to send parameters to web service
        HashMap<String, String> postDataParams;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            postDataParams = new HashMap<>();
            postDataParams.put("id", String.valueOf(++userid));
            postDataParams.put("latitude", String.valueOf(latitude));
            postDataParams.put("longitude", String.valueOf(longitude));
            postDataParams.put("datetime", datetime.toString());



            //Call ServerData() method to call webservice and store result in response
            response = service.ServerData(path,postDataParams);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(success==1) {
                Toast.makeText(getApplicationContext(), "Location Added successfully..!", Toast.LENGTH_LONG).show();
            }
        }
    }
}





