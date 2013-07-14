package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class CricketUpdates extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_updates);
        
        
        new Thread() {
        	  public void run() {
        		  getCricketJSON();
        	  }
        	}.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cricket_updates, menu);
        return true;
    }
    
    public void getCricketJSON() {
        StringBuilder builder = new StringBuilder();
         
        try {
          HttpClient client = new DefaultHttpClient();
          HttpGet httpGet = new HttpGet("http://cdnedge.bbc.co.uk/shared/app/pulsar/assets/?channel=bbc.cps.asset.23293800_HighWeb&sort=date_descending&limit=15");
       
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
            builder.append(assets.length());

            ListView UpdatesList = (ListView) findViewById(R.id.updates);
            Context UpdatesContext = UpdatesList.getContext();
            JSONAdapter adapter = JSONAdapter(UpdatesContext, assets);
            UpdatesList.setAdapter(adapter);
          }
        }
        catch(Exception e) {
          e.printStackTrace();
          builder.append(e);
        }
         
        final String str = builder.toString();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(CricketUpdates.this, str, Toast.LENGTH_LONG).show();
          }
        });
      }

    
}
