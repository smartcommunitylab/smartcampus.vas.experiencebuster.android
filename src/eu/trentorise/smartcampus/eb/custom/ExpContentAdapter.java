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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.eb.Constants;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.capture.ContentRenderer;
import eu.trentorise.smartcampus.eb.custom.capture.ResourceHandler;
import eu.trentorise.smartcampus.eb.model.Content;

public class ExpContentAdapter extends ArrayAdapter<Content> {

	private Activity context;
	private int layoutResourceId;
	private List<ResourceHandler> resourceHandlers = new ArrayList<ResourceHandler>();

	public ExpContentAdapter(Activity context, int layoutResourceId, List<Content> list) {
		super(context, layoutResourceId, list);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Content content = getItem(position);

		ContentPlaceholder tag = null;
		
		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			tag = new ContentPlaceholder();
			tag.content_frame = (FrameLayout)row.findViewById(R.id.exp_content);
			tag.content_time = (TextView) row.findViewById(R.id.exp_time);
			tag.content_note_tv = (TextView) row.findViewById(R.id.exp_notes);
			row.setTag(tag);
		} else {
			tag = (ContentPlaceholder) row.getTag();
		}

		if (position % 2 == 1) {
			row.setBackgroundResource(0);
			tag.content_note_tv.setTextColor(context.getResources().getColor(android.R.color.black));
			tag.content_time.setTextColor(context.getResources().getColor(android.R.color.black));
		} else {
			row.setBackgroundResource(R.drawable.border);
			tag.content_note_tv.setTextColor(context.getResources().getColor(android.R.color.white));
			tag.content_time.setTextColor(context.getResources().getColor(android.R.color.white));
		}
		
		tag.content_frame.removeAllViews();
		ResourceHandler rh = ContentRenderer.render(content,tag.content_frame,position);
		if (rh != null) resourceHandlers.add(rh);
		
		if (content.getNote() != null) {
			tag.content_note_tv.setVisibility(View.VISIBLE);
			tag.content_note_tv.setText(content.getNote());
		} else {
			tag.content_note_tv.setVisibility(View.GONE);
		}
		tag.content_time.setText(Constants.DATE_FORMATTER.format(new Date(content.getTimestamp())));
		return row;
	}

	public void release() {
		for (ResourceHandler rh : resourceHandlers) {
			rh.release();
		}
	}
	
	private static class ContentPlaceholder {
		TextView content_time;
		FrameLayout content_frame;
		TextView content_note_tv;
	}
}
