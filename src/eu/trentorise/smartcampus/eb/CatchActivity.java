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
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.Constants.CATCH_TYPES;
import eu.trentorise.smartcampus.eb.custom.capture.CaptureHelper;
import eu.trentorise.smartcampus.eb.custom.capture.CaptureHelper.ResultHandler;
import eu.trentorise.smartcampus.eb.custom.capture.GrabbedContent;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.BackListener;
import eu.trentorise.smartcampus.eb.fragments.NearMeNowFragment;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DialogCallbackContainer;
import eu.trentorise.smartcampus.eb.fragments.experience.EditExpFragment;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;

public class CatchActivity extends SherlockFragmentActivity implements
		ResultHandler, DialogCallbackContainer {

	public static final String ARG_TYPE = "type";

	private CaptureHelper mHelper = null;

	private Boolean initialized = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("initialized")) {
			initialized = savedInstanceState.getBoolean("initialized");
		}
		if (!initialized) {
			mHelper = new CaptureHelper(this, 0, this);
			initDataManagement(savedInstanceState);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("initialized", initialized);
	}

	@Override
	protected void onResume() {
		if(EBHelper.getLocationHelper()!=null){
			try {
				EBHelper.init(this.getApplicationContext());
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		EBHelper.getLocationHelper().start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		EBHelper.getLocationHelper().stop();
		super.onPause();
	}

	private void startCapture() {
		EBHelper.getLocationHelper().start();

		CATCH_TYPES type = null;
		if (getIntent() != null) {
			String s = getIntent().getStringExtra(ARG_TYPE);
			if (s != null) {
				type = CATCH_TYPES.valueOf(s);
			}
		}
		if (type != null) {
			if (CATCH_TYPES.TEXT.equals(type)) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				Fragment frag = new EditExpFragment();
				ft.replace(android.R.id.content, frag)
						.commitAllowingStateLoss();
			} else if (CATCH_TYPES.NEARME.equals(type)) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				Fragment frag = new NearMeNowFragment();
				ft.replace(android.R.id.content, frag)
						.commitAllowingStateLoss();
			} else {
				mHelper.startCapture(type);
			}
		} else {
			finish();
		}
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			EBHelper.init(getApplicationContext());
			initData();
		} catch (Exception e) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}

	private boolean initData() {
		try {
			startCapture();
			initialized = true;

		} catch (Exception e1) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mHelper == null) {
			mHelper = new CaptureHelper(this, 0, this);
		}
		mHelper.onCaptureResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					EBHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData();
				}
			} else if (resultCode == RESULT_CANCELED) {
				EBHelper.endAppFailure(this, R.string.token_required);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResult(GrabbedContent value) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment frag = new EditExpFragment();
		frag.setArguments(EditExpFragment.prepareArgs(null, value));
		ft.replace(android.R.id.content, frag, "grabbed")
				.commitAllowingStateLoss();
	}

	@Override
	public void onCancel() {
		finish();
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
	public CollectionSavedHandler getCollectionSavedHandler() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public AssignCollectionsCallback getAssignCollectionsCallback() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public RemoveCallback getRemoveCallback() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public NoteHandler getNoteHandler() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public PositionHandler getPositionHandler() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

	@Override
	public OnTagsSelectedListener getTagListener() {
		return (EditExpFragment) getSupportFragmentManager().findFragmentById(
				android.R.id.content);
	}

}
