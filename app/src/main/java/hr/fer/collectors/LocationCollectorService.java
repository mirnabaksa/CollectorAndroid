package hr.fer.collectors;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import hr.fer.connection.HTTPURLConnection;

public class LocationCollectorService
        extends Service {
    private final String ADDRESS_UNKNOWN = "Unknown";
    private final String SERVER_PATH = "https://zavradmb2018.000webhostapp.com/addLocation.php";

    private FusedLocationProviderClient mFusedLocationClient;
    private HTTPURLConnection service;

    private int userid = 0;
    private double latitude = 0;
    private double longitude = 0;
    private Date datetime = new Date();
    private String address;
    private Location currentLocation;

    private  LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Geocoder geocoder;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        service = new HTTPURLConnection();
        geocoder = new Geocoder(this, Locale.getDefault());

        createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if(location == null && currentLocation == null &&
                            location.getLatitude() == currentLocation.getLatitude()
                            && location.getLongitude() == currentLocation.getLongitude()) break;

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
        mLocationRequest.setInterval(60 *1000);
        mLocationRequest.setFastestInterval(60 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
        currentLocation = location;

        latitude = location.getLatitude();
        longitude= location.getLongitude();
        datetime = new Date();
        address = fetchAddress(location);
    }

    private String fetchAddress(Location location){
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
        }

        if (addresses == null || addresses.size()  == 0) {
            return ADDRESS_UNKNOWN;
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            String strAdr = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
            return strAdr;
        }
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
            postDataParams.put("address", address);

            response = service.ServerData(SERVER_PATH,postDataParams);
            return null;
        }


    }
}





