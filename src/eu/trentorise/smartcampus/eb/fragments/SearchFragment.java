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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;

public class SearchFragment extends SherlockListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.setListAdapter(new ArrayAdapter<String>(getSherlockActivity(),R.layout.search_row, getResources().getStringArray(R.array.searchArray)));
		return inflater.inflate(R.layout.search, null);
	}

	@Override
	public void onStart() {
		super.onStart();
		getView().findViewById(R.id.search_img).setOnClickListener(buttonClickListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.title_search);
	}

	@Override
	public void onListItemClick(ListView listView, View containerView, int position, long duration) {
//		// TODO for other search types
//		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
//		Fragment f = new ExperiencesListFragment();
//		Bundle b = new Bundle();
//		ExperienceFilter filter = new ExperienceFilter();
//		b.putSerializable(ExperiencesListFragment.ARG_FILTER, filter);
//		f.setArguments(b);
//		ft.replace(android.R.id.content, f);
//		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//		ft.addToBackStack(null);
//		ft.commit();
		Toast.makeText(getSherlockActivity(), "Coming soon!", Toast.LENGTH_SHORT).show();
	}
	
	OnClickListener buttonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			EditText txt = (EditText)getView().findViewById(R.id.search);
			InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

			FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			Fragment f = new ExperiencesListFragment();
			Bundle b = new Bundle();
			ExperienceFilter filter = new ExperienceFilter();
			if (txt.getText() != null) {
				filter.setText(txt.getText().toString());
			} else {
				filter.setText("");
			}
			b.putSerializable(ExperiencesListFragment.ARG_FILTER, filter);
			f.setArguments(b);
			ft.replace(R.id.content_frame, f);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(null);
			ft.commit();
		}
	};
}
