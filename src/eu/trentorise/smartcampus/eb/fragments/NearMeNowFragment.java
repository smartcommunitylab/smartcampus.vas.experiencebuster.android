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
package eu.trentorise.smartcampus.eb.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.NearMeAdapter;
import eu.trentorise.smartcampus.eb.custom.Utils;
import eu.trentorise.smartcampus.eb.custom.capture.CaptureHelper.ResultHandler;
import eu.trentorise.smartcampus.eb.custom.capture.content.ObjectContent;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.NearMeObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class NearMeNowFragment extends SherlockListFragment {

	private static final String P_FILTER_EVENTS = "filterEvents";
	private static final String P_FILTER_LOCATIONS = "filterLocations";
	private static final String P_LIST = "list";
	
	private NearMeAdapter mAdapter = null;
	private List<NearMeObject> list = null;
	
	boolean filterEvents = true;
	boolean filterLocations = true;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nearme, container, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		if (arg0 != null && arg0.containsKey(P_LIST)) {
			list = (ArrayList<NearMeObject>) arg0.getSerializable(P_LIST);
		}
		if (arg0 != null && arg0.containsKey(P_FILTER_EVENTS)) {
			filterEvents = arg0.getBoolean(P_FILTER_EVENTS, true);
		}
		if (arg0 != null && arg0.containsKey(P_FILTER_LOCATIONS)) {
			filterLocations = arg0.getBoolean(P_FILTER_LOCATIONS, true);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		if (list != null) arg0.putSerializable(P_LIST, new ArrayList<NearMeObject>(list));
		arg0.putBoolean(P_FILTER_EVENTS, filterEvents);
		arg0.putBoolean(P_FILTER_LOCATIONS, filterLocations);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			break;
		default:
			break;
		}	
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().setTitle(R.string.title_nearme);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (list != null) {
			mAdapter = new NearMeAdapter(getActivity(), R.layout.nearme_row, list);
			setListAdapter(mAdapter);
			checkListEmpty(list);
		} else {
			mAdapter = new NearMeAdapter(getActivity(), R.layout.nearme_row);
			setListAdapter(mAdapter);
			new LoadSuggestionsTask().execute();
		}
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				assert getActivity() instanceof ResultHandler;
				NearMeObject o = list.get(position);
				((ResultHandler)getActivity()).onResult(new ObjectContent(o));
			}
		});
		
		final ImageButton loc = (ImageButton)getView().findViewById(R.id.filters_near);
		if (filterLocations) loc.setImageResource(R.drawable.ic_position_s);
		else loc.setImageResource(R.drawable.ic_position);
		final ImageButton evt = (ImageButton)getView().findViewById(R.id.filters_now);
		if (filterEvents) evt.setImageResource(R.drawable.ic_date_s);
		else evt.setImageResource(R.drawable.ic_date);

		loc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filterLocations = !filterLocations;
				if (filterLocations) loc.setImageResource(R.drawable.ic_position_s);
				else loc.setImageResource(R.drawable.ic_position);
				new LoadSuggestionsTask().execute();
			}
		});
		evt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filterEvents = !filterEvents;
				if (filterEvents) evt.setImageResource(R.drawable.ic_date_s);
				else evt.setImageResource(R.drawable.ic_date);
				new LoadSuggestionsTask().execute();
			}
		});
		
	}


	private void checkListEmpty(List<NearMeObject> result) {
		((ViewGroup)getListView().getParent()).removeView(getView().findViewById(R.id.content_empty));
		if (result == null || result.isEmpty()) {
			TextView view = new TextView(getActivity());
			view.setId(R.id.content_empty);
			view.setText(R.string.content_empty);
			view.setPadding(5, 5, 5, 5);
			((ViewGroup)getListView().getParent()).addView(view);
		}
	}


	private class LoadSuggestionsTask extends SCAsyncTask<Void, Void, List<NearMeObject>> {

		public LoadSuggestionsTask() {
			super(getSherlockActivity(), 
					new AbstractAsyncTaskProcessor<Void, List<NearMeObject>>(getSherlockActivity()) {

						@Override
						public List<NearMeObject> performAction(Void... params) throws SecurityException, ConnectionException, Exception {
							GeoPoint location = Utils.requestMyLocation(getActivity());
							if (location == null) return Collections.emptyList();
							
							return EBHelper.getNearMeNowSuggestions(new double[]{location.getLatitudeE6() / 1E6, location.getLongitudeE6() / 1E6}, filterEvents, filterLocations);
						}

						@Override
						public void handleResult(List<NearMeObject> result) {
							list = result == null ? new ArrayList<NearMeObject>() : result;
							mAdapter.clear();
							mAdapter.addAll(result);
							mAdapter.notifyDataSetChanged();
							checkListEmpty(result);

						}
				
			});
		}
		
	}
	
}
