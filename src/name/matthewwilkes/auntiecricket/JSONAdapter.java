package name.matthewwilkes.auntiecricket;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
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
    TextView textView = (TextView) rowView.findViewById(R.id.label);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    try {
		textView.setText(values.get(position).toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    // Change the icon for Windows and iPhone
    //String s = values[position];
    //if (s.startsWith("iPhone")) {
      imageView.setImageResource(R.drawable.ic_launcher);
    //} else {
      //imageView.setImageResource(R.drawable.ok);
   // }

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