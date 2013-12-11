package eu.trentorise.smartcampus.eb;

import java.util.List;

import eu.trentorise.smartcampus.eb.model.ExpCollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NavDrawerAdapter extends ArrayAdapter<ExpCollection> {

	public NavDrawerAdapter(Context context,List<ExpCollection> objects) {
		super(context, R.layout.drawer_list_item, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView==null){
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView=inflater.inflate(R.layout.drawer_list_item, parent,false);
		}
		((TextView)convertView).setText(getItem(position).getName());
		return convertView;
	}
	
}
