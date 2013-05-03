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

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.BackListener;
import eu.trentorise.smartcampus.eb.fragments.MainFragment;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DialogCallbackContainer;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.sync.service.SyncStorageService;

public class HomeActivity extends SherlockFragmentActivity implements
		DialogCallbackContainer {

	protected final int mainlayout = android.R.id.content;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			EBHelper.init(getApplicationContext());
			String token = EBHelper.getAccessProvider()
					.getAuthToken(this, null);
			if (token != null) {
				initData(token);
			}
		} catch (Exception e) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}

	private boolean initData(String token) {
		try {
			new SCAsyncTask<Void, Void, Void>(this, new StartProcessor(this))
					.execute();
		} catch (Exception e1) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		initDataManagement(savedInstanceState);
		if (savedInstanceState == null) {
			setUpContent();
		}

		// ContentResolver.isSyncActive(EBHelper.SCAccount,
		// "eu.trentorise.smartcampus.eb");
		// ContentResolver.requestSync(EBHelper.SCAccount,
		// "eu.trentorise.smartcampus.eb", new Bundle());

		// EBHelper.synchronize();
	}

	private void setUpContent() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment frag = null;
		frag = new MainFragment();
		ft.replace(android.R.id.content, frag).commitAllowingStateLoss();
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
				if (token == null) {
					EBHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData(token);
				}
			} else if (resultCode == RESULT_CANCELED) {
				EBHelper.endAppFailure(this,
						eu.trentorise.smartcampus.ac.R.string.token_required);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

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
			EBHelper.getAccessProvider().invalidateToken(HomeActivity.this,
					null);
			initDataManagement(null);
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
