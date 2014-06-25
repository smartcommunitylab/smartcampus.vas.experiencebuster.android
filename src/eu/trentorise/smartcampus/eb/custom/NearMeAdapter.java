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

import it.smartcampuslab.eb.R;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.eb.model.NearMeObject;

public class NearMeAdapter extends ArrayAdapter<NearMeObject> {

	private Activity context;
	private int layoutResourceId;

	public NearMeAdapter(Activity context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	public NearMeAdapter(Activity context, int layoutResourceId,
			List<NearMeObject> list) {
		super(context, layoutResourceId, list);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		NearMeObject tag = null;

		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			row.setTag(getItem(position));
		}
		tag = getItem(position);

		((TextView) row.findViewById(R.id.nearme_name)).setText(tag.getTitle());
		((TextView) row.findViewById(R.id.nearme_loc)).setText(tag
				.descriptionString());
		return row;
	}

}
