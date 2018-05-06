package hr.fer.connection;

import android.os.AsyncTask;

import java.util.HashMap;

public class PostDataToServer extends AsyncTask<Void, Void, Void> {
    private HashMap<String, String> postDataParams;
    private String serverPath;
    private HTTPURLConnection conn;

    public PostDataToServer(String serverPath, HashMap<String,String> postDataParams){
        this.postDataParams = postDataParams;
        this.serverPath = serverPath;
        this.conn = new HTTPURLConnection();
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    @Override
    protected Void doInBackground(Void... arg0) {
        String response = conn.ServerData(serverPath, postDataParams);
        return null;
    }


}
