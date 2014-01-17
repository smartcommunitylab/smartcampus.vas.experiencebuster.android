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
package eu.trentorise.smartcampus.eb;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.ListViewTutorialHelper;
import com.github.espiandev.showcaseview.TutorialHelper;
import com.github.espiandev.showcaseview.TutorialHelper.TutorialProvider;
import com.github.espiandev.showcaseview.TutorialItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.BackListener;
import eu.trentorise.smartcampus.eb.fragments.ExperiencesListFragment;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DialogCallbackContainer;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.eb.model.UserPreference;
import eu.trentorise.smartcampus.eb.syncadapter.FileSyncService;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class HomeActivity extends SherlockFragmentActivity implements
		DialogCallbackContainer, OnItemClickListener, OnItemLongClickListener {

	public interface RefreshCallback {
		public void refresh(String id, String name, boolean animate);
	}

	protected final int mainlayout = R.id.content_frame;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private ListView mListView;
	private ArrayList<ExpCollection> collections;

	private TutorialHelper mTutorialHelper = null;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			EBHelper.init(getApplicationContext());
			if (!EBHelper.getAccessProvider().login(this, null)) {
				initData();
			}
		} catch (Exception e) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}

	private boolean initData() {
		try {
			// // check filestorage account
			if (!EBHelper.ensureSyncConfig(this)) {
				new SCAsyncTask<Void, Void, Void>(this,
						new StartProcessor(this)).execute();
			}
		} catch (Exception e1) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, FileSyncService.class));
		// supportInvalidateOptionsMenu();
		setContentView(R.layout.base);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		initDataManagement(savedInstanceState);
		mTutorialHelper = new ListViewTutorialHelper(this, mTutorialProvider);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerLayout == null)
			setupNavDrawer();

	}

	private void setUpContent() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment frag = null;

		// frag = new MainFragment();
		frag = new ExperiencesListFragment();

		ft.replace(R.id.content_frame, frag).commitAllowingStateLoss();

		setupNavDrawer();

		findViewById(R.id.drawer_uncantorized).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						refreshFragment(null, null, true);
					}
				});

		prepareButtons();
	}

	private void prepareButtons() {
		TextView add = (TextView) findViewById(R.id.drawer_add_category);
		add.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fm = getSupportFragmentManager();
				NewCollectionDialogFragment dialog = new NewCollectionDialogFragment();
				dialog.show(fm, "dialog");
			}
		});

	}

	public void showTutorial() {
		mTutorialHelper.showTutorials();
	}

	private void setupNavDrawer() {

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// this is a class created to avoid an Android bug
		// see the class for further infos.
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.app_name,
				R.string.app_name) {

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				refreshMenuList();
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mListView = (ListView) findViewById(R.id.drawer_list);
		collections = new ArrayList<ExpCollection>();
		mListView.setAdapter(new NavDrawerAdapter(this, collections));
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	private void readCollections() {
		UserPreference userPreference = EBHelper.getUserPreference();
		if (collections == null)
			collections = new ArrayList<ExpCollection>();
		else
			collections.clear();
		if (userPreference.getCollections() != null) {
			collections.addAll(userPreference.getCollections());
		}
	}

	public void refreshMenuList() {
		readCollections();
		if (mListView != null)
			((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		// Checking if there is a fragment that it's listening for back button
		if (currentFragment != null && currentFragment instanceof BackListener) {
			((BackListener) currentFragment).onBack();
		} else {
			super.onBackPressed();
		}
		mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mTutorialHelper.onTutorialActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				EBHelper.endAppFailure(this, R.string.app_failure_security);
				initData();
			} else if (resultCode == RESULT_CANCELED) {
				EBHelper.endAppFailure(this, R.string.token_required);
			}
		}
		if (requestCode == EBHelper.FILESTORAGE_ACCOUNT_REGISTRATION) {
			EBHelper.handleAccountActivityResult(this, requestCode, resultCode,
					data);
			new SCAsyncTask<Void, Void, Void>(this, new StartProcessor(this))
					.execute();

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout != null)
				if (mDrawerLayout
						.isDrawerOpen(findViewById(R.id.drawer_wrapper))) {
					mDrawerLayout
							.closeDrawer(findViewById(R.id.drawer_wrapper));
				} else {
					mDrawerLayout.openDrawer(findViewById(R.id.drawer_wrapper));
				}
			return true;
		case R.id.mainmenu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
		default:
			if (mDrawerLayout.isDrawerOpen(findViewById(R.id.drawer_wrapper))) {
				mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
			}
			return super.onOptionsItemSelected(item);
		}

	}

	// TODO enable for synchronization configuration
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getSupportMenuInflater().inflate(R.menu.main_menu, menu);
	// return true;
	// }

	private class StartProcessor extends AbstractAsyncTaskProcessor<Void, Void> {

		public StartProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Void performAction(Void... params) throws SecurityException,
				Exception {
			EBHelper.start();
			return null;
		}

		@Override
		public void handleResult(Void result) {
			setUpContent();
			readCollections();
			refreshMenuList();
			if (mDrawerLayout != null) {
				mDrawerToggle.syncState();
			}
		}

	}

	@Override
	public CollectionSavedHandler getCollectionSavedHandler() {
		return (CollectionSavedHandler) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
	}

	@Override
	public AssignCollectionsCallback getAssignCollectionsCallback() {
		return (AssignCollectionsCallback) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
	}

	@Override
	public RemoveCallback getRemoveCallback() {
		return (RemoveCallback) getSupportFragmentManager().findFragmentById(
				R.id.content_frame);
	}

	@Override
	public NoteHandler getNoteHandler() {
		return (NoteHandler) getSupportFragmentManager().findFragmentById(
				R.id.content_frame);
	}

	@Override
	public PositionHandler getPositionHandler() {
		return (PositionHandler) getSupportFragmentManager().findFragmentById(
				R.id.content_frame);
	}

	@Override
	public OnTagsSelectedListener getTagListener() {
		return (OnTagsSelectedListener) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		refreshFragment(collections.get(position).getId(),
				collections.get(position).getName(), true);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			final int arg2, long arg3) {
		final ExpCollection coll = EBHelper.getUserPreference()
				.getCollections().get(arg2);

		new AlertDialog.Builder(HomeActivity.this)
				.setMessage(R.string.msg_delete_coll_confirm)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								EBHelper.getUserPreference().getCollections()
										.remove(coll);
								if (EBHelper.updateUserPreference(
										HomeActivity.this,
										EBHelper.getUserPreference())) {
									collections.remove(arg2);
									((ArrayAdapter) mListView.getAdapter())
											.notifyDataSetChanged();
									refreshFragment(null, null, false);
								} else {
									EBHelper.getUserPreference()
											.getCollections().add(arg2, coll);
								}
							}
						}).setNegativeButton(android.R.string.no, null).show();
		return true;
	}

	private void refreshFragment(String id, String name, boolean animate) {
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		if (currentFragment != null
				&& currentFragment instanceof RefreshCallback) {
			((RefreshCallback) currentFragment).refresh(id, name, animate);
		} else {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			Fragment frag = new ExperiencesListFragment();
			if (id != null) {
				ExperienceFilter ef = new ExperienceFilter();
				ef.setCollectionIds(new String[] { id });
				Bundle b = new Bundle();
				b.putSerializable(ExperiencesListFragment.ARG_FILTER, ef);
				frag.setArguments(b);
			}

			ft.replace(R.id.content_frame, frag).commitAllowingStateLoss();
		}
		mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
	}

	private TutorialProvider mTutorialProvider = new TutorialProvider() {

		TutorialItem[] tutorial = new TutorialItem[] {
				new TutorialItem("grab", null, 0, R.string.t_title_grab,
						R.string.t_msg_grab),
				new TutorialItem("search", null, 0, R.string.t_title_search,
						R.string.t_msg_search),
				new TutorialItem("categories", null, 0, R.string.t_title_cat,
						R.string.t_msg_cat),
		// new TutorialItem("settings", null, 0,
		// R.string.t_title_settings, R.string.t_msg_settings),
		};

		@Override
		public int size() {
			return tutorial.length;
		}

		@Override
		public void onTutorialFinished() {
			mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
		}

		@Override
		public void onTutorialCancelled() {
			mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
		}

		@Override
		public TutorialItem getItemAt(int pos) {
			mDrawerLayout.openDrawer(findViewById(R.id.drawer_wrapper));
			View v = null;
			switch (pos) {
			case 0:
				v = findViewById(R.id.expmenu_add);
				break;
			case 1:
				v = findViewById(R.id.expmenu_search);
				break;
			case 2:
				v = findViewById(R.id.drawer_wrapper).findViewById(
						R.id.logo_collections);
				break;
			default:
				break;
			}
			if (v != null) {
				tutorial[pos].position = new int[2];
				v.getLocationOnScreen(tutorial[pos].position);
				tutorial[pos].width = v.getWidth();

				// In the navigation drawer there is
				// some padding that influence the position
				if (pos == 2) {
					tutorial[pos].position[0] -= EBHelper.convertPixelsToDp(20,
							HomeActivity.this);
					tutorial[pos].position[1] -= EBHelper.convertPixelsToDp(20,
							HomeActivity.this);
					tutorial[pos].width = v.getWidth()
							+ (int) EBHelper.convertPixelsToDp(40,
									HomeActivity.this);
				} else {
					tutorial[pos].width = v.getWidth();
				}
			}
			return tutorial[pos];
		}
	};

}
