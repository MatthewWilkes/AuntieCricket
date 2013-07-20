package name.matthewwilkes.auntiecricket;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JSONAdapter extends BaseAdapter {
  private final Context context;
  private final JSONArray values;


  public class URLDrawable extends BitmapDrawable {
      // the drawable that you need to set, you could set the initial drawing
      // with the loading image if you need to
      protected Drawable drawable;

      @Override
      public void draw(Canvas canvas) {
          // override the draw to facilitate refresh function later
          if(drawable != null) {
              drawable.draw(canvas);
          }
      }
  }
  public class URLImageParser implements ImageGetter {
      Context c;
      View container;

      /***
       * Construct the URLImageParser which will execute AsyncTask and refresh the container
       * @param t
       * @param c
       */
      public URLImageParser(View t, Context c) {
          this.c = c;
          this.container = t;
      }

      public Drawable getDrawable(String source) {
          URLDrawable urlDrawable = new URLDrawable();

          // get the actual source
          ImageGetterAsyncTask asyncTask = 
              new ImageGetterAsyncTask( urlDrawable);

          asyncTask.execute(source);

          // return reference to URLDrawable where I will change with actual image from
          // the src tag
          return urlDrawable;
      }

      public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
          URLDrawable urlDrawable;

          public ImageGetterAsyncTask(URLDrawable d) {
              this.urlDrawable = d;
          }

          @Override
          protected Drawable doInBackground(String... params) {
              String source = params[0];
              return fetchDrawable(source);
          }

          @Override
          protected void onPostExecute(Drawable result) {
              // set the correct bound according to the result from HTTP call
              urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0 
                      + result.getIntrinsicHeight()); 

              // change the reference of the current drawable to the result
              // from the HTTP call
              urlDrawable.drawable = result;

              // redraw the image by invalidating the container
              URLImageParser.this.container.invalidate();
          }

          /***
           * Get the Drawable from URL
           * @param urlString
           * @return
           */
          public Drawable fetchDrawable(String urlString) {
              try {
                  InputStream is = fetch(urlString);
                  Drawable drawable = Drawable.createFromStream(is, "src");
                  drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0 
                          + drawable.getIntrinsicHeight()); 
                  return drawable;
              } catch (Exception e) {
                  return null;
              } 
          }

          private InputStream fetch(String urlString) throws MalformedURLException, IOException {
              DefaultHttpClient httpClient = new DefaultHttpClient();
              HttpGet request = new HttpGet(urlString);
              HttpResponse response = httpClient.execute(request);
              return response.getEntity().getContent();
          }
      }
  }
  
  public JSONAdapter(Context context, JSONArray values) {
    this.context = context;
    this.values = values;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
    
    TextView title = (TextView) rowView.findViewById(R.id.title);
    TextView story = (TextView) rowView.findViewById(R.id.story);
    ImageView image = (ImageView) rowView.findViewById(R.id.icon);
    
    try {
    	JSONObject newsitem = (JSONObject) values.get(position);
    	JSONObject content = (JSONObject) newsitem.get("content");
    	JSONObject message = (JSONObject) content.get("message");
    	
    	String text = message.get("text").toString();
    	
    	URLImageParser p = new URLImageParser((View) story, context);
		story.setText(Html.fromHtml(text, p, null));
		
		String title_text = message.get("head").toString();
		if (title_text.length() > 1) {
			title_text = title_text.substring(0, 1) + title_text.substring(1).toLowerCase();
		}
		
		if (title_text.equals("{}")) {
			title.setVisibility(View.GONE);
		}
		title.setText(title_text);
		
		if (title_text.equals("Wicket")) {
			image.setImageResource(R.drawable.ic_wicket);
		}
		else { 
			image.setImageResource(R.drawable.ic_launcher);
		}
		
    	String subhead = message.get("subhead").toString();

		if (!subhead.equals("{}"))
		{
			title.setText(title_text + ": " + subhead);
		}

		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       
    return rowView;
  }


@Override
public int getCount() {
	return this.values.length();
}


@Override
public Object getItem(int index) {
	try {
		return this.values.get(index);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return e;
	}
}


@Override
public long getItemId(int index) {
	return index;
}

} 