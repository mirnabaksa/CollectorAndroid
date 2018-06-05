package hr.fer;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

import hr.fer.collectors.AudioCollectorService;
import hr.fer.collectors.KeyboardCollectorService;
import hr.fer.collectors.LocationCollectorService;

import hr.fer.R;

public class MainActivity extends AppCompatActivity {
    private final static int WAIT_INTERVAL = 10 * 1000; //10 seconds
    private final static int ACCOUNT_CODE = 1;

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
    private Intent accountIntent;

    private String accountName;
    private TextView accountHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }

        accountHolder = findViewById(R.id.account);
        if(savedInstanceState != null){
            accountName = savedInstanceState.getString("account");
            accountHolder.setText(accountName);
        }else{
            accountIntent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[] {"com.google"},
                    false, null, null, null, null);
            startActivityForResult(accountIntent, ACCOUNT_CODE);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("account", accountName);
    }

    private void configureIntents(){
        locationIntent = new Intent(this, LocationCollectorService.class);
        locationIntent.putExtra("Account", accountName);

        keyboardIntent = new Intent(this, KeyboardCollectorService.class);
        keyboardIntent.putExtra("Account", accountName);

        audioIntent = new Intent(this, AudioCollectorService.class);
        audioIntent.putExtra("Account", accountName);
    }

    private void configureButtons(){
        locationButton = findViewById(R.id.locationButton);
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

        keyboardButton = findViewById(R.id.keyboardButton);
        keyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyboard = !keyboard;

                String text;
                if(keyboard){
                    text = "Stop";

                    AlertDialog.Builder builder;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(MainActivity.this);
                    }
                    builder.setTitle("Set Keyboard")
                            .setMessage("In order for the Keyboard Collector to work, you need to set the current " +
                                    "keyboard to Collector IME in your settings. Ignore if you have already done this! ")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                                    startActivity(settingsIntent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();

                }else{
                    text = "Start";
                }

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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
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
           this.startService(intent);
       } else {
           this.stopService(intent);
       }
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACCOUNT_CODE) {
            if (resultCode == RESULT_OK) {
                this.accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                accountHolder.setText("Using: " + this.accountName);
                configureIntents();
                configureButtons();
            } else if (resultCode == RESULT_CANCELED) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Warning")
                        .setMessage("In order to use this app, you need to select an account!")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(accountIntent, ACCOUNT_CODE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }
    }

}


