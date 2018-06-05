package hr.fer.collectors;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;


import hr.fer.connection.PostDataToServer;

public class AudioCollectorService extends Service {
    private static final String FILE_NAME = "audio";
    private static final String FILE_EXT = ".3gp";
    private final String SERVER_PATH = "http://collector-env-1.2ta8wpyecx.us-east-2.elasticbeanstalk.com/audio/store";

    private String account;
    private String mFileName = null;
    private String bytes;
    private String serverFileName;
    private MediaRecorder mRecorder = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        account = (String) intent.getExtras().get("Account");
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/" + FILE_NAME + FILE_EXT;
        serverFileName = FILE_NAME + "-" + System.currentTimeMillis() + FILE_EXT;;

        startRecording();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Audio", "prepare() failed");

        }
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        byte[] bytes = getBytes(mFileName);
        this.bytes = new String(bytes);
        new PostDataToServer(SERVER_PATH, preparePOSTParams()).execute();

        mRecorder.release();
        mRecorder = null;
    }

    private byte[] getBytes(String path) {
        byte[] getBytes = {};
        try {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBytes;
    }

    private HashMap<String, String> preparePOSTParams(){
        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("path", serverFileName);
        postDataParams.put("date", new Date().toString());
        postDataParams.put("account", account);
        postDataParams.put("bytes", bytes);

        return postDataParams;
    }

}



