package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class DownloadCricketService extends IntentService {


	public DownloadCricketService() {
		super("DownloadCricketService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

        Intent mIntent = new Intent(CricketUpdates.START_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = Integer.parseInt(prefs.getString("Magic number", "23302781"));
        System.err.println(id);
        String url = "http://cdnedge.bbc.co.uk/shared/app/pulsar/assets/?channel=bbc.cps.asset." + (id + 1) + "_HighWeb&sort=date_descending&limit=5";
        JSONArray data = getCricketJSON(url);
        
        mIntent = new Intent(CricketUpdates.RECEIVE_JSON);
        Bundle mBundle = new Bundle();
        mBundle.putString("data", data.toString());
        mIntent.putExtras(mBundle);
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
	}
	
    public JSONArray getCricketJSON(String url) {
        StringBuilder builder = new StringBuilder();
         
        try {
          HttpClient client = new DefaultHttpClient();
          
          HttpGet httpGet = new HttpGet(url);
       
          HttpResponse response = client.execute(httpGet);
          int statusCode = response.getStatusLine().getStatusCode();
           
          if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            InputStreamReader reader = new InputStreamReader(content);
            BufferedReader buffered = new BufferedReader(reader);
            String line;
            while ((line = buffered.readLine()) != null) {
              builder.append(line);
            }
            JSONObject cricket = new JSONObject(builder.toString());
            
            builder = new StringBuilder();
            JSONArray assets = (JSONArray) ((JSONObject) cricket.get("assetlist")).get("assets");
            return assets;
          }
        }
        catch(Exception e) {
          e.printStackTrace();
          builder.append(e);
        }
		return null;
      }


}
