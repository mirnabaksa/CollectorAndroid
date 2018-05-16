package hr.fer.collectors;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;


import hr.fer.connection.PostDataToServer;

public class AudioCollectorService extends Service {
    private static final String FILE_NAME = "audio";
    private static final String FILE_EXT = ".3gp";
    private final String SERVER_PATH = "http://collector-env-1.2ta8wpyecx.us-east-2.elasticbeanstalk.com/audio/store";
    private final String FTP_PATH = "files.000webhost.com";
    private final String username = "zavradmb2018";
    private final String pass =  "zavrsnirad2018";

    private String account;
    private String mFileName = null;
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
        new PostDataToServer(SERVER_PATH, preparePOSTParams()).execute();

        new UploadFileAsync(mFileName).execute();
        mRecorder.release();
        mRecorder = null;
    }

    private HashMap<String, String> preparePOSTParams() {
        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("path", serverFileName);
        postDataParams.put("date", new Date().toString());
        postDataParams.put("account", account);
        return postDataParams;
    }


    class UploadFileAsync extends AsyncTask<String, Void, String> {

        private String filePath;

        public UploadFileAsync(String path) {
            Log.d("upload", "async");
            this.filePath = path;
        }

        @Override
        protected String doInBackground(String... params) {
            FTPClient con = null;
            try
            {
                con = new FTPClient();
                con.connect(FTP_PATH);
                con.changeWorkingDirectory("/records");


                if (con.login(username,pass))
                {
                    con.enterLocalPassiveMode();
                    con.setFileType(FTP.BINARY_FILE_TYPE);
                    String data = filePath;

                    FileInputStream in = new FileInputStream(new File(data));
                    boolean result = con.storeFile(serverFileName, in);
                    in.close();
                    if (result) Log.v("Audio", "Upload succeeded");

                    con.logout();
                    con.disconnect();
                }
            }
            catch (Exception e)
            {
                Log.v("Audio","Upload failed");
                e.printStackTrace();
            }
            return null;
        }

    }
}



