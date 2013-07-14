package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

public class CricketUpdates extends Activity {

	private class DownloadCricketTask extends AsyncTask<Void, Void, JSONArray> {
    	private long lastCricketNotificationTime;

		protected JSONArray doInBackground(Void... params) {
            return getCricketJSON();
        }
        
    	protected void onPreExecute() {
    		findViewById(R.id.working).setVisibility(View.VISIBLE);

    		SharedPreferences settings = getPreferences(0);
            this.lastCricketNotificationTime = settings.getLong("lastCricketNotification", 0);
    	}
    	
        protected void onPostExecute(JSONArray result) {
            ListView UpdatesList = (ListView) findViewById(R.id.updates);
            Context UpdatesContext = UpdatesList.getContext();
            BaseAdapter adapter = new JSONAdapter(UpdatesContext, result);
            UpdatesList.setAdapter(adapter);
            
            
            SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-mm-dd:kk:mm:ss");

            for (int i = result.length(); i >= 0; --i) {
                JSONObject message;
                
                Date date = new Date();
                
				try {
					message = result.getJSONObject(i);

	                try {
	        			date = parserSDF.parse(message.get("createtime").toString().replace("T",":"));
	        		} catch (ParseException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	                
	                
	                if (this.lastCricketNotificationTime > date.getTime())
	                	continue;
	                
	                this.lastCricketNotificationTime  = date.getTime();
	                String msg_type = ((JSONObject) ((JSONObject) message.get("content")).get("message")).get("type").toString();
	                System.err.println(msg_type);
	                if (msg_type.equals("STANDARD")) {
	                	continue;
	                }
	                else if (msg_type.equals("TWEET")) {
	                	continue;
	                }
	                else if (msg_type.equals("EMAIL")) {
	                	continue;
	                }
	                else if (msg_type.equals("SMS")) {
	                	continue;
	                }
	                else if (msg_type.equals("HANDOVER")) {
	                	continue;
	                }
	                else if (msg_type.equals("WICKET")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("DROPPED_CATCH")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("UMPIRE_REVIEW")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("NOT_OUT")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("APPEAL_NOT_OUT")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("DROPPED_CATCH")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("INTERVAL")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("RUNS_50")) {
	                	notifyJSON(message);
	                }
	                else if (msg_type.equals("RUNS_100")) {
	                	notifyJSON(message);
	                }
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            SharedPreferences settings = getPreferences(0);
            Editor editor = settings.edit();
            editor.putLong("lastCricketNotification", this.lastCricketNotificationTime);
            editor.commit();
            
            findViewById(R.id.working).setVisibility(View.INVISIBLE);
            //Toast.makeText(CricketUpdates.this,
            //        String.valueOf(result.toString()), Toast.LENGTH_LONG).show();
            }

    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_updates);
       
        DownloadCricketTask download = new DownloadCricketTask();
		download.execute();
    }


    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cricket_updates, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.wantsRefresh:
                DownloadCricketTask download = new DownloadCricketTask();
        		download.execute();
                return true;
            case R.id.notify:
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void notifyJSON(JSONObject notify) throws JSONException {
    	JSONObject content = (JSONObject) notify.get("content");
        //Toast.makeText(CricketUpdates.this,
        //        String.valueOf(notify.toString()), Toast.LENGTH_LONG).show();
        
        Spanned full_text = Html.fromHtml(((JSONObject) content.get("message")).get("text").toString());
        
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-mm-dd:kk:mm:ss");
        Date date = new Date();
        try {
			date = parserSDF.parse(notify.get("createtime").toString().replace("T",":"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	NotificationCompat.Builder mBuilder =
    			new NotificationCompat.Builder(this)
    			.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(((JSONObject) content.get("message")).get("head").toString())
                .setContentText(full_text)
                .setWhen(date.getTime())
                .setStyle(new NotificationCompat.BigTextStyle()
                	.bigText(full_text));
    	NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.notify((int) date.getTime(), mBuilder.build());

    }
    
    public JSONArray getCricketJSON() {
        StringBuilder builder = new StringBuilder();
         
        try {
          HttpClient client = new DefaultHttpClient();
          
          int id = 23302781;
          String url = "http://cdnedge.bbc.co.uk/shared/app/pulsar/assets/?channel=bbc.cps.asset." + (id + 1) + "_HighWeb&sort=date_descending&limit=5";
          
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
