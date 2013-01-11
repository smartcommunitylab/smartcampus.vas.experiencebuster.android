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
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.eb.model.UserPreference;

public class CollectionsListFragment extends SherlockListFragment implements CollectionSavedHandler {

	private List<ExpCollection> collections = null;
	private static final String STATE_ITEMS = "items";
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setHasOptionsMenu(true);
		if (arg0 != null && arg0.containsKey(STATE_ITEMS)) {
			collections = (List<ExpCollection>)arg0.getSerializable(STATE_ITEMS);
		} else {
			collections = new ArrayList<ExpCollection>();
			readCollections();
		}
	}
	private void readCollections() {
		UserPreference userPreference = EBHelper.getUserPreference();
		collections.clear();
		if (userPreference.getCollections() != null) {
			collections.addAll(userPreference.getCollections());
		}
	}
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable(STATE_ITEMS, collections == null ? null : new ArrayList<ExpCollection>(collections));
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.setListAdapter(new ArrayAdapter<ExpCollection>(getSherlockActivity(), R.layout.collections_row, collections));
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.title_collections);
		registerForContextMenu(getListView());
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.collection_add, 1, R.string.dialog_collection_add);
		item.setIcon(R.drawable.ic_add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.collection_add:
			DialogFragment newCollFragment = new NewCollectionDialogFragment();
			newCollFragment.setArguments(NewCollectionDialogFragment.prepare(null));
		    newCollFragment.show(getActivity().getSupportFragmentManager(), "exp_coll");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCollectionSaved(ExpCollection coll) {
		readCollections();
		((ArrayAdapter<ExpCollection>)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView listView, View containerView, int position, long duration) {
		FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		Fragment f = new ExperiencesListFragment();
		Bundle b = new Bundle();
		ExperienceFilter filter = new ExperienceFilter();
		filter.setCollectionId(collections.get(position).getId());
		b.putSerializable(ExperiencesListFragment.ARG_FILTER, filter);
		f.setArguments(b);
		ft.replace(android.R.id.content, f);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();

	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.coll_menu_header);
		android.view.MenuInflater inflater = getSherlockActivity().getMenuInflater();
	    inflater.inflate(R.menu.coll_list_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final ExpCollection coll = EBHelper.getUserPreference().getCollections().get(info.position);
		switch (item.getItemId()) {
		case R.id.collmenu_remove:
		{
			new AlertDialog.Builder(getActivity())
	        .setMessage(R.string.msg_delete_coll_confirm)
	        .setCancelable(false)
	        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	            @SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int id) {
	            	EBHelper.getUserPreference().getCollections().remove(coll);
	            	if (EBHelper.updateUserPreference(getSherlockActivity(), EBHelper.getUserPreference())) {
		            	collections.remove(info.position);
		            	((ArrayAdapter<ExpCollection>)getListAdapter()).notifyDataSetChanged();
	            	} else {
		            	EBHelper.getUserPreference().getCollections().add(info.position,coll);
	            	}
	            }
	        })
	        .setNegativeButton(android.R.string.no, null)
	        .show();
			break;
		}
		case R.id.collmenu_edit:
		{
			DialogFragment editCollFragment = new NewCollectionDialogFragment();
			editCollFragment.setArguments(NewCollectionDialogFragment.prepare(coll));
		    editCollFragment.show(getActivity().getSupportFragmentManager(), "exp_coll");
			break;
		}
		}		
		return super.onContextItemSelected(item);
	}
}
