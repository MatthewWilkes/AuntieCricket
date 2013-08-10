package name.matthewwilkes.auntiecricket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
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

public class FindFeed extends IntentService {


	public FindFeed() {
		super("FindFeed");
		new FindFeedsTask();
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
        System.err.println("intent");
		new FindEventsTask().execute();
	}

	   private class FindEventsTask extends AsyncTask<Void, Void, ArrayList<URI>> {

			@Override
	    	protected void onPostExecute(ArrayList<URI> result) {
				new FindFeed.FindFeedsTask().execute(result);
	    	}
			
			@Override
			protected ArrayList<URI> doInBackground(Void... arg0) {
				StringBuilder builder = new StringBuilder();
				ArrayList<URI> result = new ArrayList<URI>();

				
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet("http://www.bbc.co.uk/sport/0/cricket/");
					httpGet.addHeader("Accept-Encoding", "gzip");

					HttpResponse response = client.execute(httpGet);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						Header contentEncoding = response.getFirstHeader("Content-Encoding");
						if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
							content = new GZIPInputStream(content);
						}
						
						InputStreamReader reader = new InputStreamReader(content);
						BufferedReader buffered = new BufferedReader(reader);
						String line;
						while ((line = buffered.readLine()) != null) {
							builder.append(line+"\n");
						}
						
						String url;
						Pattern pattern = Pattern.compile("<h2 class=\"headline-live\">[ \n]*<a href=\"(.*?)\">");
						Matcher matcher = pattern.matcher(builder.toString());
						while (matcher.find()) {
							result.add(URI.create("http://www.bbc.co.uk" + matcher.group(1)));
						}

					}
				}
				catch(Exception e) {
					e.printStackTrace();
					builder.append(e);
				}
				return result;
			}


	    }	
	
    private class FindFeedsTask extends AsyncTask<ArrayList<URI>, Void, ArrayList<java.util.HashMap<String, String>>> {

		protected void onPreExecute() {
		}
		
		@Override
    	protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
    		System.err.println(result.toString());
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context) getApplicationContext());
			Editor editor = settings.edit();
    		if (!result.isEmpty()) {
    			editor.putString("feed", result.get(0).get("id"));
        		Intent intent = new Intent(getApplicationContext(), DownloadCricketService.class);
        		startService(intent);
    		} else {
    			editor.putString("feed", "");
            	Toast.makeText(getApplicationContext(),
                        String.valueOf("Couldn't find any live events."), Toast.LENGTH_LONG).show(); 	        

    		}
			editor.commit();
    	}

		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(ArrayList<URI>... arg0) {
	        StringBuilder builder = new StringBuilder();
	        ArrayList<HashMap<String, String>> result = new ArrayList();
	        for (int i=0; i<arg0[0].size();i++) {
	        	result.add((getInfoForURL(arg0[0].get(i))));
	        }
			return result;
					
		}

		protected HashMap<String, String> getInfoForURL(URI url) {
			StringBuilder builder = new StringBuilder();
			String id=null;
			String headline=null;
			HashMap<String, String> result = new HashMap<String, String>();
			// TODO Auto-generated method stub
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader("Accept-Encoding", "gzip");

				HttpResponse response = client.execute(httpGet);
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					Header contentEncoding = response.getFirstHeader("Content-Encoding");
					if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
						content = new GZIPInputStream(content);
					}
					
					InputStreamReader reader = new InputStreamReader(content);
					BufferedReader buffered = new BufferedReader(reader);
					String line;
					while ((line = buffered.readLine()) != null) {
						builder.append(line+"\n");
					}

					Pattern pattern = Pattern.compile("PULSAR_CHANNEL:bbc.cps.asset.(.+?)_HighWeb\n");
					Matcher matcher = pattern.matcher(builder.toString());
					while (matcher.find()) {
						id = matcher.group(1);
					}

					pattern = Pattern.compile("<meta name=\"Headline\" content=\"(.+?)\"/>");
					matcher = pattern.matcher(builder.toString());
					while (matcher.find()) {
						headline = matcher.group(1);
					}


				}
			}
			catch(Exception e) {
				e.printStackTrace();
				builder.append(e);
			}
			result.put("id", id);
			result.put("headline", headline);

			return result;
		}
		
    }
}
 