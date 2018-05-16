package hr.fer.connection;


import android.text.TextUtils;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

/**
 * NEEDS REWRITING
 */
public class HTTPURLConnection {
    private static final String LINE_FEED = "\r\n";
    private String response="";
    private String boundary;
    private URL url;

    public String ServerData(String path, HashMap<String, String> params) {
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

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            String paramsString = getPostDataString(params);

            writer.write(paramsString);

            writer.flush();
            writer.close();
            os.close();


            int responseCode = connection.getResponseCode();
            response = String.valueOf(responseCode);
         }catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder writer = new StringBuilder();

        for(Map.Entry<String, String> entry : params.entrySet()){
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=" + "UTF-8").append(
                    LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(entry.getValue());
            writer.append(LINE_FEED);

        }

        writer.append(LINE_FEED);
        writer.append("--" + boundary + "--").append(LINE_FEED);
        return writer.toString();
    }
}
