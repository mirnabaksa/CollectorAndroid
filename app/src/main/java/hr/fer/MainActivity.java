package hr.fer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import hr.fer.collectors.AudioCollectorService;
import hr.fer.collectors.KeyboardCollectorService;
import hr.fer.collectors.LocationCollectorService;

import hr.fer.keyboard.R;

public class MainActivity extends AppCompatActivity {
    private final static int WAIT_INTERVAL = 10 * 1000; //10 seconds
    private Intent mediaIntent;

    private Button recordButton;
    private Button keyboardButton;
    private Button locationButton;

    private String locationButtonText = " Location Collector";
    private String recordButtonText = " Audio Collector";
    private String keyboardButtonText = " Keyboard Collector";

    private boolean location = false;
    private boolean keyboard = false;
    private boolean audio = false;

    private Intent locationIntent;
    private Intent keyboardIntent;
    private Intent audioIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }

        configureIntents();
        configureButtons();
    }

    private void configureIntents(){
        locationIntent = new Intent(this, LocationCollectorService.class);
        keyboardIntent = new Intent(this, KeyboardCollectorService.class);
        audioIntent = new Intent(this, AudioCollectorService.class);
    }

    private void configureButtons(){
        locationButton = (Button) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location = !location;

                String text;
                if(location){
                    text = "Stop";
                }else text = "Start";


                locationButton.setText(text + locationButtonText);
                manageService(locationIntent, location);
            }
        });

        keyboardButton = (Button) findViewById(R.id.keyboardButton);
        keyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyboard = !keyboard;

                String text;
                if(keyboard){
                    text = "Stop";
                }else text = "Start";

                keyboardButton.setText(text + keyboardButtonText);
                manageService(keyboardIntent, keyboard);
            }
        });

        recordButton = (Button) findViewById(R.id.audioButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audio = !audio;

                String text;
                if(audio){
                    text = "Stop";
                }else text = "Start";

                recordButton.setText(text + recordButtonText);
                manageService(audioIntent, audio);
            }
        });


    }

    public void checkPermission() {
        while(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){

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

   private void manageService(Intent intent, boolean start) {
       if (start) {
           startService(intent);
       } else {
           stopService(intent);
       }
   }



}


