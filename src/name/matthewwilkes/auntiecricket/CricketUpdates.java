package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Html.ImageGetter;
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


	public static final String START_UPDATE = "name.matthewwilkes.auntiecricket.START_UPDATE";
	public static final String RECEIVE_JSON = "name.matthewwilkes.auntiecricket.RECEIVE_JSON";

	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if(intent.getAction().equals(START_UPDATE)) {
	            findViewById(R.id.working).setVisibility(View.VISIBLE);
	        }
	        else if(intent.getAction().equals(RECEIVE_JSON)) {
	            Bundle result = intent.getExtras();
	            JSONArray data;
				try {
					data = new JSONArray(result.getString("data"));
		            BaseAdapter adapter = new JSONAdapter(context, data);

		            ListView UpdatesList = (ListView) findViewById(R.id.updates);
		            Context UpdatesContext = UpdatesList.getContext();
		            UpdatesList.setAdapter(adapter);
		            findViewById(R.id.working).setVisibility(View.INVISIBLE);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	};

	
	/*private class DownloadCricketTask extends AsyncTask<Object, Void, JSONArray> {
    	private long lastCricketNotificationTime;
		private Object context;

		protected JSONArray doInBackground(Object... params) {
			this.context = params[0];
            
            return getCricketJSON(url);
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

    }*/	
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_updates);

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_JSON);
        intentFilter.addAction(START_UPDATE);
        bManager.registerReceiver(bReceiver, intentFilter);
        
		Intent RegularUpdate = new Intent(this, DownloadCricketService.class);
    	PendingIntent pending = PendingIntent.getService(getApplicationContext(), 0, RegularUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()-500, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pending);
        
        // Make sure it happens the first time
        startService(RegularUpdate);
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
    	Intent intent;
        switch (item.getItemId()) {
            case R.id.wantsRefresh:
            	intent = new Intent(this, DownloadCricketService.class);
            	startService(intent);
                return true;
            case R.id.action_settings:
            	intent = new Intent(this, SettingsActivity.class);
            	startActivity(intent);
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
    

    
}
