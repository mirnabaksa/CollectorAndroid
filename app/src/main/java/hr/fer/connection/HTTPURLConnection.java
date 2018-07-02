package hr.fer.connection;


import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.data;

public class HTTPURLConnection {
    private static final String LINE_FEED = "\r\n";
    private String response="";
    private String boundary;
    private URL url;

    public String send(String path, HashMap<String, String> params) {
        try {
            url = new URL(path);
            boundary = "===" + System.currentTimeMillis() + "===";

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(1500000);
            connection.setConnectTimeout(15000000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

            writeToStream(dos, params);

            dos.flush();
            dos.close();

            int responseCode = connection.getResponseCode();
            response = String.valueOf(responseCode);
            Log.d("respo", response);
         }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private void writeToStream(DataOutputStream dos, HashMap<String, String> params) throws IOException {
        for(Map.Entry<String, String> entry : params.entrySet()){
            dos.writeBytes("--" + boundary + LINE_FEED);


            if(entry.getKey().equals("audiofilepath")) {
                dos.writeBytes("Content-Disposition: post-data; name=audiofilepath;filename="
                                + params.get("path") + "" + LINE_FEED);
                dos.writeBytes(LINE_FEED);
                writeAudio(dos, entry.getValue());
                dos.writeBytes(LINE_FEED);
            }
            else {
                dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED);
                dos.writeBytes("Content-Type: text/plain; charset=" + "UTF-8" + LINE_FEED);
                dos.writeBytes(LINE_FEED);
                dos.writeBytes(entry.getValue());
                dos.writeBytes(LINE_FEED);
            }

        }
        dos.writeBytes(LINE_FEED);
       dos.writeBytes("--" + boundary + "--" + LINE_FEED);


    }


    private void writeAudio(DataOutputStream dos, String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        int bytesAvailable = fileInputStream.available();
        int bufferSize = Math.min(bytesAvailable, 1024);
        byte[] buffer = new byte[bufferSize];
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0)
        {
            dos.write(buffer, 0, bytesRead);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, 1024);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

    }
}
