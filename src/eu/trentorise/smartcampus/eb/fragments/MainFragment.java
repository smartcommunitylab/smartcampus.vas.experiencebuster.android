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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.MainAdapter;

public class MainFragment extends SherlockFragment {

	private GridView gridview;

	private int currentlyOpened = -1;
	private MainAdapter mAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null) currentlyOpened = savedInstanceState.getInt("current", -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.main, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.app_name);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		// inflater.inflate(R.menu.main_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.mainmenu_settings:
		// startActivity(new Intent(getActivity(), SettingsActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		gridview = (GridView) getView().findViewById(R.id.gridview);
		mAdapter = new MainAdapter(getSherlockActivity().getApplicationContext(), getSherlockActivity().getSupportFragmentManager());
		gridview.setAdapter(mAdapter);
		if (currentlyOpened >= 0) mAdapter.setCurrentlyOpened(currentlyOpened);
	}

	@Override
	public void onPause() {
		super.onPause();
		Fragment opened = getSherlockActivity().getSupportFragmentManager().findFragmentById(android.R.id.content); 
		if (opened != null && !(opened instanceof MainFragment)) {
			this.currentlyOpened = mAdapter.getCurrentlyOpened();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putInt("current",currentlyOpened);
	}
	
}
