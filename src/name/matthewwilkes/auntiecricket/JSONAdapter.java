package name.matthewwilkes.auntiecricket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JSONAdapter extends BaseAdapter {
  private final Context context;
  private final JSONArray values;

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
		story.setText(Html.fromHtml(text));
		
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