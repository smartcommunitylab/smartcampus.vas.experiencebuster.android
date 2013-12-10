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

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.filestorage.FilestorageAccountActivity;
import eu.trentorise.smartcampus.eb.fragments.BackListener;
import eu.trentorise.smartcampus.eb.fragments.ExperiencesListFragment;
import eu.trentorise.smartcampus.eb.fragments.MainFragment;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DialogCallbackContainer;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.sync.service.SyncStorageService;

public class HomeActivity extends SherlockFragmentActivity implements
		DialogCallbackContainer {

	private final static int FILESTORAGE_ACCOUNT_REGISTRATION = 10000;

	protected final int mainlayout = android.R.id.content;

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private FragmentManager mFragmentManager;

	private ListView mListView;

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

			// TODO uncomment this to enable synchronization
			// // check filestorage account
			// if (EBHelper.getConfiguration(EBHelper.CONF_SYNCHRO,
			// Boolean.class)
			// && EBHelper.getConfiguration(EBHelper.CONF_USER_ACCOUNT,
			// String.class) == null) {
			// EBHelper.askUserAccount(this, FILESTORAGE_ACCOUNT_REGISTRATION,
			// true);
			// } else {
			new SCAsyncTask<Void, Void, Void>(this, new StartProcessor(this))
					.execute();
			// }
		} catch (Exception e1) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// supportInvalidateOptionsMenu();
		setContentView(R.layout.base);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		initDataManagement(savedInstanceState);
		if (savedInstanceState == null) {
			setUpContent();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	private void setUpContent() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment frag = null;

		//frag = new MainFragment();
		frag = new ExperiencesListFragment();

		ft.replace(R.id.content_frame, frag).commitAllowingStateLoss();
		
		setupNavDrawer();
	}

	private void setupNavDrawer() {

		mFragmentManager = getSupportFragmentManager();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// this is a class created to avoid an Android bug
		// see the class for further infos.
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.app_name, R.string.app_name);

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mListView = (ListView) findViewById(R.id.drawer_list);

		mListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new ArrayList<String>()));

	}

	@Override
	public void onBackPressed() {
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
		// Checking if there is a fragment that it's listening for back button
		if (currentFragment != null && currentFragment instanceof BackListener) {
			((BackListener) currentFragment).onBack();
		}

		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				EBHelper.endAppFailure(this, R.string.app_failure_security);
				initData();
			} else if (resultCode == RESULT_CANCELED) {
				EBHelper.endAppFailure(this, R.string.token_required);
			}
		}
		if (requestCode == FILESTORAGE_ACCOUNT_REGISTRATION) {
			if (resultCode == Activity.RESULT_OK) {
				String accountId = data
						.getStringExtra(FilestorageAccountActivity.EXTRA_USER_ACCOUNT_ID);
				try {
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, true,
							Boolean.class);
					EBHelper.saveConfiguration(EBHelper.CONF_USER_ACCOUNT,
							accountId, String.class);

				} catch (DataException e) {
					Toast.makeText(getApplicationContext(),
							"Error saving filestorage account",
							Toast.LENGTH_SHORT).show();
					Log.e(HomeActivity.class.getName(),
							"Error saving filestorage account");
				}
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				try {
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, false,
							Boolean.class);
				} catch (DataException e) {
					Log.e(HomeActivity.class.getName(),
							"Error saving filestorage account");
				}
			}
			new SCAsyncTask<Void, Void, Void>(this, new StartProcessor(this))
					.execute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(findViewById(R.id.drawer_wrapper))) {
	            mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
	        } else {
	            mDrawerLayout.openDrawer(findViewById(R.id.drawer_wrapper));
	        }
			return true;
		case R.id.mainmenu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
		default:
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
		}

	}

	private BroadcastReceiver mTokenInvalidReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(
				SyncStorageService.ACTION_AUTHENTICATION_PROBLEM);
		registerReceiver(mTokenInvalidReceiver, filter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mTokenInvalidReceiver);
		super.onPause();
	}

	@Override
	public CollectionSavedHandler getCollectionSavedHandler() {
		return (CollectionSavedHandler) getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
	}

	@Override
	public AssignCollectionsCallback getAssignCollectionsCallback() {
		return (AssignCollectionsCallback) getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
	}

	@Override
	public RemoveCallback getRemoveCallback() {
		return (RemoveCallback) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public NoteHandler getNoteHandler() {
		return (NoteHandler) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public PositionHandler getPositionHandler() {
		return (PositionHandler) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public OnTagsSelectedListener getTagListener() {
		return (OnTagsSelectedListener) getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
	}

}
