/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.custom;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class OptionsListAdapter extends ArrayAdapter<String> {

	Context context;
	int layoutResourceId;
	List<String> contentsList;

	public OptionsListAdapter(Context context, int layoutResourceId, List<String> optionsList) {
		super(context, layoutResourceId, optionsList);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.contentsList = optionsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.optionTextView = (TextView) row.findViewById(android.R.id.text1);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		String content = contentsList.get(position);
		holder.optionTextView.setText(content);

		// Log.e(this.getClass().getSimpleName(), "ROW DONE");
		return row;
	}

	static class DataHolder {
		TextView optionTextView;
	}
}
