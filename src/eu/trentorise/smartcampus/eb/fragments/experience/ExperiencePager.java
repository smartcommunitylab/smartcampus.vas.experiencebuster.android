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
package eu.trentorise.smartcampus.eb.fragments.experience;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import it.smartcampuslab.eb.R;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.eb.model.Experience;

public class ExperiencePager extends SherlockFragmentActivity implements
		DialogCallbackContainer {

	private static final String ARG_POSITION = "position";
	public static final String ARG_COLLECTION = "coll";

	private ArrayList<Experience> collection = null;

	private ViewPager mPager = null;
	private ExperiencePagerAdapter mAdapter = null;

	private String currentId = null;

	HashMap<String, WeakReference<EditExpFragment>> fragMap = new HashMap<String, WeakReference<EditExpFragment>>();

	public static Intent prepareIntent(Context ctx, Integer position,
			ArrayList<Experience> collection) {

		Intent i = new Intent(ctx, ExperiencePager.class);
		if (position != null)
			i.putExtra(ARG_POSITION, position);
		if (collection != null)
			i.putExtra(ARG_COLLECTION, collection);
		return i;
	}

	private EditExpFragment current(String id) {
		if (fragMap.containsKey(id))
			return fragMap.get(id).get();
		return null;
	}

	@Override
	public void onAttachFragment(Fragment f) {
		if (f instanceof EditExpFragment) {
			fragMap.put(((Experience) ((EditExpFragment) f).getArguments()
					.getSerializable(EditExpFragment.ARG_EXP)).getId(),
					new WeakReference<EditExpFragment>((EditExpFragment) f));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exp_fragment_pager);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(ARG_COLLECTION)) {
			collection = (ArrayList<Experience>) savedInstanceState
					.getSerializable(ARG_COLLECTION);
		} else if (getIntent() != null) {
			collection = (ArrayList<Experience>) getIntent()
					.getSerializableExtra(ARG_COLLECTION);
		} else {
			collection = new ArrayList<Experience>();
		}
		mAdapter = new ExperiencePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		int currentIdx = getIntent().getIntExtra(ARG_POSITION, 0);
		mPager.setCurrentItem(currentIdx);
		setCurrentIdFromIdx(currentIdx);

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int currentIdx) {
				setCurrentIdFromIdx(currentIdx);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private void setCurrentIdFromIdx(int currentIdx) {
		if (collection != null && collection.size() > currentIdx) {
			currentId = collection.get(currentIdx).getId();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// savedInstanceState.putInt(ARG_POSITION, position);
		savedInstanceState.putSerializable(ARG_COLLECTION, collection);
	}

	@Override
	public void onBackPressed() {
		updateResult();
		super.onBackPressed();
	}

	private void updateResult() {
		if (collection != null) {
			for (Iterator<Experience> iterator = collection.iterator(); iterator
					.hasNext();) {
				Experience e = iterator.next();
				if (e.markedDeleted()) {
					iterator.remove();
				}
				fragMap.remove(e.getId());
			}
		}
		setResult(RESULT_OK, new Intent().putExtra(ARG_COLLECTION, collection));
	}

	@Override
	public CollectionSavedHandler getCollectionSavedHandler() {
		return current(currentId);
	}

	@Override
	public AssignCollectionsCallback getAssignCollectionsCallback() {
		return current(currentId);
	}

	@Override
	public RemoveCallback getRemoveCallback() {
		return current(currentId);
	}

	@Override
	public NoteHandler getNoteHandler() {
		return current(currentId);
	}

	@Override
	public PositionHandler getPositionHandler() {
		return current(currentId);
	}

	@Override
	public OnTagsSelectedListener getTagListener() {
		return current(currentId);
	}

	private class ExperiencePagerAdapter extends FragmentStatePagerAdapter {
		public ExperiencePagerAdapter(FragmentManager arg0) {
			super(arg0);
		}

		@Override
		public Fragment getItem(int position) {
			EditExpFragment frag = new EditExpFragment();
			frag.setArguments(EditExpFragment.prepareArgs(
					collection.get(position), null));
			return frag;
		}

		@Override
		public int getCount() {
			return collection.size();
		}
	}
}
