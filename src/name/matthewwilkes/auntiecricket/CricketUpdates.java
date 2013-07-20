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

	
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_updates);

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_JSON);
        intentFilter.addAction(START_UPDATE);
        bManager.registerReceiver(bReceiver, intentFilter);
        
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        
		Intent RegularUpdate = new Intent(this, DownloadCricketService.class);
    	PendingIntent pending = PendingIntent.getService(getApplicationContext(), 0, RegularUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean("pref_sync", true)) {
	        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()-500, (30*1000), pending);
        }
        
        // Make sure it happens the first time
    	Intent intent = new Intent(this, FindFeed.class);
    	startService(intent);
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
            case R.id.findFeed:
            	intent = new Intent(this, FindFeed.class);
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
    
    

    
}
