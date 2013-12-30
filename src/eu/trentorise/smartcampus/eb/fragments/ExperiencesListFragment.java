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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.eb.HomeActivity;
import eu.trentorise.smartcampus.eb.HomeActivity.RefreshCallback;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.SettingsActivity;
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
		eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback,
		RefreshCallback, BackListener {
	
	

	public static final String ARG_FILTER = "filter";

	private static final String STATE_ITEMS = "items";

	private static final int REQUEST_CODE_PAGER = 35;

	private List<Experience> experiencesList;
	private static ExperienceFilter filter;

	private static final int ACCOUNT_CREATION = 10000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// if (savedInstanceState != null
		// && savedInstanceState.containsKey(STATE_ITEMS)) {
		// experiencesList = (List<Experience>) savedInstanceState
		// .getSerializable(STATE_ITEMS);
		// } else if (getArguments() != null
		// && getArguments().containsKey(ARG_FILTER)) {
		// filter = (ExperienceFilter) getArguments().getSerializable(
		// ARG_FILTER);
		// experiencesList = EBHelper.findExperiences(filter, 0, -1);
		// } else {
		// // experiencesList = EBHelper.getExperiences(0, -1);
		experiencesList = new ArrayList<Experience>(1);
		// }
		setHasOptionsMenu(true);

		}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.exp_list_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mainmenu_settings:
			startActivity(new Intent(getActivity(), SettingsActivity.class));
			break;
		case R.id.expmenu_add:
			FragmentManager fm = getFragmentManager();
			DialogFragment f = new GrabDialogFragment();
			f.show(fm, "grabdialog");
			break;
		case R.id.expmenu_search:
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.content_frame, new SearchFragment(), "search");
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(null);
			ft.commit();
			
			//this is just to manage the back button
			//because the search can show a subset of items
			filter= new ExperienceFilter();
			break;
		case R.id.expmenu_settings:
			startActivity(new Intent(getActivity(),
					SettingsActivity.class));
			break;
		case R.id.expmenu_tutorial:
			if(getActivity() instanceof HomeActivity)
				((HomeActivity)getActivity()).showTutorial();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
	public void onStart() {
		super.onStart();
		this.getListView().setDivider(getResources().getDrawable(R.drawable.border));
		this.getListView().setDividerHeight(1);
		animateList();

		// hide keyboard if opened
		InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		keyboard.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);

	}

	private void animateList() {
		getListView().clearAnimation();
		getView().postDelayed(new Runnable() {

			@Override
			public void run() {
				TranslateAnimation animation = new TranslateAnimation(0, 0,
						200, 0);
				animation.setDuration(300);
				animation.setFillBefore(true);
				getListView().startAnimation(animation);
			}
		}, 50);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerForContextMenu(getListView());

		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);

		experiencesList.clear();

		if (getArguments() != null && getArguments().containsKey(ARG_FILTER)) {
			filter = (ExperienceFilter) getArguments().getSerializable(
					ARG_FILTER);
			experiencesList.addAll(EBHelper.findExperiences(filter, 0, -1));
		} else {
			filter = null;
			experiencesList.addAll(EBHelper.getExperiences(0, -1));
		}
		if (filter == null) {
			getSherlockActivity().getSupportActionBar().setTitle(
					R.string.title_diary);
		} else if (filter.getCollectionIds() != null
				&& filter.getCollectionIds().length > 0) {
			ExpCollection c = EBHelper
					.findCollection(filter.getCollectionIds()[0]);
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

		getSherlockActivity().getSupportActionBar().setIcon(
				R.drawable.ic_launcher);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);

		((ArrayAdapter) getListView().getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.exp_menu_header);
		android.view.MenuInflater inflater = getSherlockActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.exp_list_context_menu, menu);

		android.view.MenuItem item = menu.findItem(R.id.expmenu_share);
		if (item != null) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			Experience exp = experiencesList.get(info.position);
			item.setEnabled(exp.getEntityId() != null).setVisible(
					exp.getEntityId() != null);
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
		case R.id.expmenu_share:
			EBHelper.share(exp, getActivity());
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
		//TODO uncomment this to enable synchronization
//		try {
//			if (EBHelper.getConfiguration(EBHelper.CONF_USER_ACCOUNT,
//					String.class) == null) {
//				EBHelper.askUserAccount(getActivity(), ACCOUNT_CREATION, true);
//			}
//		} catch (DataException e1) {
//			Log.e(ExperiencesListFragment.class.getName(),
//					"Error creating filestorage user account");
//		}
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

	@Override
	public void refresh(String id,String name,boolean animate) {
		experiencesList.clear();
		if (id != null) {
			ExperienceFilter ef = new ExperienceFilter();
			ef.setCollectionIds(new String[] { id });
			filter=ef;
			experiencesList = EBHelper.findExperiences(ef, 0, -1);
			getSherlockActivity().getSupportActionBar().setTitle(name);
		} else {
				experiencesList=EBHelper.getExperiences(0, -1);
				getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.mainmenu_diary));
			filter=null;
		}
		this.setListAdapter(new ExperiencesListAdapter(getSherlockActivity(),
				R.layout.experience_row, experiencesList));
		((ArrayAdapter) getListView().getAdapter()).notifyDataSetChanged();
		if(animate)
			animateList();
	}

	@Override
	public void onBack() {
		if (filter!=null) {
			if(!filter.isEmpty())
				experiencesList = EBHelper.findExperiences(filter, 0, -1);
			else{
				experiencesList = EBHelper.getExperiences(0, -1);
				filter=null;
			}
			this.setListAdapter(new ExperiencesListAdapter(getSherlockActivity(),
					R.layout.experience_row, experiencesList));
			((ArrayAdapter) getListView().getAdapter()).notifyDataSetChanged();
			getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.mainmenu_diary));
			animateList();
		} else {
			getActivity().finish();
		}
	}
}
