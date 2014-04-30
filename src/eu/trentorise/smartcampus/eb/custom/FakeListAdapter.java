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
import it.smartcampuslab.eb.R;
import eu.trentorise.smartcampus.eb.model.Fake;

public class FakeListAdapter extends ArrayAdapter<Fake> {

	private Context context;
	private int layoutResourceId;
	private List<Fake> contentsList;

	public FakeListAdapter(Context context, int layoutResourceId, List<Fake> contentsList) {
		super(context, layoutResourceId, contentsList);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.contentsList = contentsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.separator = (TextView) row.findViewById(R.id.separator);

			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		Fake fake = contentsList.get(position);
		Fake previousFake = null;
		if (position - 1 >= 0) {
			previousFake = contentsList.get(position - 1);
		}

		if (previousFake == null || fake.getDate() != previousFake.getDate()) {
			holder.separator.setText("Day " + Long.toString(fake.getDate()));
			holder.separator.setVisibility(View.VISIBLE);
		} else {
			holder.separator.setVisibility(View.GONE);
		}


		return row;
	}

	private static class Holder {
		TextView separator;
	}

}
