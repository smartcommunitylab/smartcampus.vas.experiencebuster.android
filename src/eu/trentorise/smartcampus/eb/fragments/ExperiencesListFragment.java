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
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.ExperiencesListAdapter;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.EditExpFragment;
import eu.trentorise.smartcampus.eb.fragments.experience.ExperiencePager;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.storage.DataException;

public class ExperiencesListFragment extends SherlockListFragment
		implements
		RemoveCallback,
		eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback {

	public static final String ARG_FILTER = "filter";

	private static final String STATE_ITEMS = "items";

	private static final int REQUEST_CODE_PAGER = 35;

	private List<Experience> experiencesList;
	private ExperienceFilter filter;

	private static final int ACCOUNT_CREATION = 10000;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ITEMS)) {
			experiencesList = (List<Experience>) savedInstanceState
					.getSerializable(STATE_ITEMS);
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_FILTER)) {
			filter = (ExperienceFilter) getArguments().getSerializable(
					ARG_FILTER);
			experiencesList = EBHelper.findExperiences(filter, 0, -1);
		} else {
			experiencesList = EBHelper.getExperiences(0, -1);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable(STATE_ITEMS, experiencesList == null ? null
				: new ArrayList<Experience>(experiencesList));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setListAdapter(new ExperiencesListAdapter(getSherlockActivity(),
				R.layout.experience_row, experiencesList));
		return inflater.inflate(R.layout.explist, null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = ExperiencePager.prepareIntent(getActivity(), position,
				new ArrayList<Experience>(experiencesList));
		startActivityForResult(i, REQUEST_CODE_PAGER);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerForContextMenu(getListView());

		// Showing/hiding back button
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);
		if (filter == null) {
			getSherlockActivity().getSupportActionBar().setTitle(
					R.string.title_diary);
		} else if (filter.getCollectionId() != null) {
			ExpCollection c = EBHelper.findCollection(filter.getCollectionId());
			if (c != null)
				getSherlockActivity().getSupportActionBar().setTitle(
						c.getName());
		} else if (filter.getText() != null) {
			getSherlockActivity().getSupportActionBar().setTitle(
					"Search for '" + filter.getText() + "'");
		} else {
			getSherlockActivity().getSupportActionBar().setTitle(
					R.string.title_search);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.exp_menu_header);
		android.view.MenuInflater inflater = getSherlockActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.exp_list_menu, menu);
		MenuItem item = menu.findItem(R.id.expmenu_share);
		if (item != null) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			Experience exp = experiencesList.get(info.position);
			item.setEnabled(exp.getEntityId() > 0).setVisible(
					exp.getEntityId() > 0);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final Experience exp = experiencesList.get(info.position);
		switch (item.getItemId()) {
		case R.id.expmenu_remove:
			DialogFragment newFragment = new DeleteExperienceFragment();
			newFragment.setArguments(DeleteExperienceFragment.prepare(exp
					.getId()));
			newFragment.show(getActivity().getSupportFragmentManager(),
					"exp_delete");
			return true;
		case R.id.expmenu_assign_collection:
			DialogFragment assignFragment = new AssignCollectionFragment();
			assignFragment.setArguments(AssignCollectionFragment.prepare(
					exp.getId(), exp.getCollectionIds()));
			assignFragment.show(getActivity().getSupportFragmentManager(),
					"exp_assign_colls");
			return true;
		default:
			Toast.makeText(getActivity(), R.string.not_implemented,
					Toast.LENGTH_SHORT).show();
			return true;
		}

	}

	@Override
	public void onRemoved(String id) {
		for (Iterator<Experience> iterator = experiencesList.iterator(); iterator
				.hasNext();) {
			Experience e = iterator.next();
			if (e.getId().equals(id)) {
				iterator.remove();
				((ExperiencesListAdapter) getListAdapter())
						.notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public void onCollectionsAssigned(String id, List<String> colls) {
		try {
			if (EBHelper.getConfiguration(EBHelper.CONF_USER_ACCOUNT,
					String.class) == null) {
				EBHelper.askUserAccount(getActivity(), ACCOUNT_CREATION, true);
			}
		} catch (DataException e1) {
			Log.e(ExperiencesListFragment.class.getName(),
					"Error creating filestorage user account");
		}
		for (Experience e : experiencesList) {
			if (e.getId().equals(id)) {
				e.setCollectionIds(colls);
				EBHelper.saveExperience(getSherlockActivity(), e, true);
				((ExperiencesListAdapter) getListAdapter())
						.notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resCode, Intent data) {
		if (reqCode == ACCOUNT_CREATION) {
			if (resCode == Activity.RESULT_OK) {
				String accountId = data.getStringExtra("USER_ACCOUNT_ID");
				try {
					EBHelper.saveConfiguration(EBHelper.CONF_USER_ACCOUNT,
							accountId, String.class);
				} catch (DataException e) {
					Log.e(EditExpFragment.class.getName(),
							"Error saving configuration: "
									+ EBHelper.CONF_USER_ACCOUNT);
				}
			}
		}

		if (REQUEST_CODE_PAGER == reqCode && Activity.RESULT_OK == resCode) {
			@SuppressWarnings("unchecked")
			ArrayList<Experience> list = (ArrayList<Experience>) data
					.getSerializableExtra(ExperiencePager.ARG_COLLECTION);
			if (list != null) {
				experiencesList.clear();
				experiencesList.addAll(list);
				((ExperiencesListAdapter) getListAdapter())
						.notifyDataSetChanged();
			}
		}
		super.onActivityResult(reqCode, resCode, data);
	}

}
