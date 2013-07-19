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

import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.eb.Constants;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;

@SuppressLint("NewApi")
public class ExperiencesListAdapter extends ArrayAdapter<Experience> {

	private Context context;
	private int layoutResourceId;
	private List<Experience> contentsList;

	public ExperiencesListAdapter(Context context, int layoutResourceId, List<Experience> contentsList) {
		super(context, layoutResourceId, contentsList);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.contentsList = contentsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ImgHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ImgHolder();
			holder.separator = (View) row.findViewById(R.id.separator);
			holder.title = (TextView) row.findViewById(R.id.title);
			holder.description = (TextView) row.findViewById(R.id.description);
			holder.collections = (TextView) row.findViewById(R.id.collections);
			holder.place = (TextView) row.findViewById(R.id.place);
			holder.date = (TextView) row.findViewById(R.id.date);
			holder.preview = (ImageView) row.findViewById(R.id.preview);

			row.setTag(holder);
		} else {
			holder = (ImgHolder) row.getTag();
		}

		Experience experience = contentsList.get(position);
		Experience previousExperience = null;
		if (position - 1 >= 0) {
			previousExperience = contentsList.get(position - 1);
		}

		if (previousExperience == null
				|| ((long) (experience.getCreationTime() / (1000 * 60 * 60 * 24))) != ((long) (previousExperience
						.getCreationTime() / (1000 * 60 * 60 * 24)))) {
			String headerDateString = formatDateForHeader(experience.getCreationTime()).toUpperCase();
			((TextView) holder.separator.findViewById(R.id.separator_text)).setText(headerDateString);
			holder.separator.setVisibility(View.VISIBLE);
		} else {
			holder.separator.setVisibility(View.GONE);
		}

		holder.title.setText(experience.getTitle());
		if (experience != null && experience.getDescription() != null && experience.getDescription().length() > 0) {
			holder.description.setText(experience.getDescription());
		} else {
			holder.description.setVisibility(View.GONE);
		}

		String collectionsString = "";
		// if (EBHelper.getUserPreference()!=null)
		collectionsString = EBHelper.getUserPreference().collectionNames(experience.getCollectionIds());

		// // set up for possible future "click on collection name to open it"
		// // implementation
		// for (String collectionName :
		// EBHelper.getUserPreference().collectionsNamesList(experience.getCollectionIds()))
		// {
		// if (collectionsString.length() > 0) {
		// collectionsString += ", ";
		// }
		// collectionsString += collectionName;
		// }

		if (collectionsString != null && collectionsString.length() > 0) {
			holder.collections.setVisibility(View.VISIBLE);
			holder.collections.setText(collectionsString);
		} else {
			holder.collections.setVisibility(View.GONE);
		}

		if (experience.preview() != null) {
			holder.preview.setVisibility(View.VISIBLE);
			holder.preview.setImageBitmap(experience.preview());
		} else {
			holder.preview.setVisibility(View.VISIBLE);
			Content previewContent = experience.computePreview();
			if (previewContent != null) {
				holder.preview.setTag(previewContent.getId());
				// if device use android 3, use parallel async execution
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					new ImageLoadTask(holder.preview, null).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,
							previewContent);
				} else {// serial otherwise
					new ImageLoadTask(holder.preview, null).execute(previewContent);
				}

			} else {
				holder.preview.setVisibility(View.GONE);
			}
		}

		holder.place.setText(experience.getAddress());
		holder.date.setText(formatDate(experience.getCreationTime()));

		return row;
	}

	public static class ImgHolder {
		View separator;
		TextView title;
		TextView description;
		TextView collections;
		TextView place;
		TextView date;
		ImageView preview;
	}

	private String formatDate(long millis) {
		return Constants.DATE_FORMATTER.format(new Date(millis));
	}

	private String formatDateForHeader(long millis) {
		return Constants.DATE_FORMATTER_HEADER.format(new Date(millis));
	}

}
