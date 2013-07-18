package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
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

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.ListView;
import android.widget.Toast;

public class DownloadCricketService extends IntentService {


	public DownloadCricketService() {
		super("DownloadCricketService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork.isConnectedOrConnecting();
		boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

		if (!isConnected) {
			return;
		}
		
		if (!isWiFi) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context) getApplicationContext());
			long lasttime = settings.getLong("lastExpensiveCheckTime", 0);
			long current = System.currentTimeMillis();
			
			/*System.err.println(lasttime);
			System.err.println(current);
			*/
			if (lasttime + (1000*60*10) > current) {
				return;
			}
			Editor edit = settings.edit();
			edit.putLong("lastExpensiveCheckTime", current);
			edit.commit();
		}
		
        Intent mIntent = new Intent(CricketUpdates.START_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = Integer.parseInt(prefs.getString("Magic number", "23309559"));
        System.err.println(id);
        String url = "http://cdnedge.bbc.co.uk/shared/app/pulsar/assets/?channel=bbc.cps.asset." + (id + 1) + "_HighWeb&sort=date_descending&limit=5";
        JSONArray data = getCricketJSON(url);
        
        if (data==null) {
        	Toast.makeText(this,
               String.valueOf("Couldn't fetch updates."), Toast.LENGTH_LONG).show(); 	        
        	return;
        }
        
        mIntent = new Intent(CricketUpdates.RECEIVE_JSON);
        Bundle mBundle = new Bundle();
        mBundle.putString("data", data.toString());
        mIntent.putExtras(mBundle);
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
        new CricketNotificationTask().execute(data);
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

    private class CricketNotificationTask extends AsyncTask<JSONArray, Void, Void> {
    	private long lastCricketNotificationTime;

		protected void onPreExecute() {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context) getApplicationContext());
			this.lastCricketNotificationTime = settings.getLong("lastCricketNotificationTime", 0);
		}
		
        protected Void doInBackground(JSONArray... result) {
            SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-mm-dd:kk:mm:ss");

            for (int i = result[0].length()-1; i >= 0; --i) {
                JSONObject message;
                
                Date date = new Date();
                
				try {
					message = result[0].getJSONObject(i);

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
            
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context) getApplicationContext());
			this.lastCricketNotificationTime = settings.getLong("lastCricketNotificationTime", 0);
            Editor editor = settings.edit();
            editor.putLong("lastCricketNotification", this.lastCricketNotificationTime);
            editor.commit();
			return null;
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

}
