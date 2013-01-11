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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment;
import eu.trentorise.smartcampus.eb.model.ExpCollection;

public class AssignCollectionFragment extends SherlockDialogFragment {

	public interface AssignCollectionsCallback {
		void onCollectionsAssigned(String id, List<String> colls);
	}

	private static final String ARG_ID = "experienceId";
	private static final String ARG_COLLS = "collections";
	
	private boolean[] selected = null;
	private Set<String> selectedIds = null;
	private String[] items = null;
	
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putBooleanArray("selected", selected == null ? new boolean[0] : selected);
		out.putStringArrayList("selectedIds", selectedIds == null ? new ArrayList<String>() : new ArrayList<String>(selectedIds));
		out.putStringArray("items", items);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final List<ExpCollection> collections = EBHelper.getUserPreference().getCollections();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.expmenu_assign_collection);
		
		if (savedInstanceState != null) {
			selected = savedInstanceState.getBooleanArray("selected"); 
			selectedIds = new HashSet<String>(savedInstanceState.getStringArrayList("selectedIds"));
			items = savedInstanceState.getStringArray("items");
		} else {
			items = new String[collections.size()];
			selected = new boolean[collections.size()];
			for (int i = 0; i < items.length; i++) {
				items[i] = collections.get(i).getName();
				boolean b = false;
				if (getArguments().getStringArrayList(ARG_COLLS) != null) {
					for(String s: getArguments().getStringArrayList(ARG_COLLS)) {
						if (s.equals(collections.get(i).getId())) {
							b = true; break;
						}
					}
				}
				selected[i] = b;
			}
			selectedIds = new HashSet<String>();
			if (getArguments().getStringArrayList(ARG_COLLS) != null) selectedIds.addAll(getArguments().getStringArrayList(ARG_COLLS));
		}
		
		
		builder.setMultiChoiceItems(items, selected, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if (isChecked) selectedIds.add(collections.get(which).getId());
				else selectedIds.remove(collections.get(which).getId());
			}
		});
		
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				exp.setCollectionIds(new ArrayList<String>(selectedIds));
//				Fragment f = getFragmentManager().findFragmentById(containerId);
				AssignCollectionsCallback c = ((DialogCallbackContainer)getActivity()).getAssignCollectionsCallback();
				c.onCollectionsAssigned(getArguments().getString(ARG_ID), new ArrayList<String>(selectedIds));
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(R.string.dialog_collection_add, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DialogFragment newCollFragment = new NewCollectionDialogFragment();
				newCollFragment.setArguments(NewCollectionDialogFragment.prepare(null));
			    newCollFragment.show(getActivity().getSupportFragmentManager(), "exp_coll");
//
//			    NewCollectionDialogFragment.saveCollectionDialog(getActivity(), null, new CollectionSavedHandler() {
//					@Override
//					public void onCollectionSaved(ExpCollection coll) {
////						exp.setCollectionIds(Collections.singletonList(coll.getId()));
//						((AssignCollectionsCallback)getFragmentManager().findFragmentById(android.R.id.content)).onCollectionsAssigned(getArguments().getString(ARG_ID), Collections.singletonList(coll.getId()));
//					}
//				}).show();
				dialog.dismiss();
			}
		});
		
		return builder.create();
	}

	public static Bundle prepare(String id, List<String> collections) {
		Bundle b = new Bundle();
		b.putString(ARG_ID, id);
		if (collections != null) {
			b.putStringArrayList(ARG_COLLS, new ArrayList<String>(collections));
		}
		return b;
	}

}
