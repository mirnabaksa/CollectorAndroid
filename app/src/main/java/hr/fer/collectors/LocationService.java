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

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;


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
    private String path = "https://zavradmb2018.000webhostapp.com/addlocation.php";

    private int userid = 0;
    private double latitude = 0;
    private double longitude = 0;

    private Date datetime = new Date();

    private  LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        service = new HTTPURLConnection();
        createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                    Toast.makeText(getApplicationContext(),"Sending to server!" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                    new PostDataTOServer().execute();
                }
            };
        };

        startLocationUpdates();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void startLocationUpdates() {
        while(true){
            try{
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null /* Looper */);
                break;
            }catch(SecurityException ex) {
                continue;
            }
        }

    }

    private void updateLocation(Location location) {
        latitude = location.getLatitude();
        longitude= location.getLongitude();
        datetime = new Date();
    }



    private class PostDataTOServer extends AsyncTask<Void, Void, Void> {
        String response = "";

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

            response = service.ServerData(path,postDataParams);
            return null;
        }


    }
}





