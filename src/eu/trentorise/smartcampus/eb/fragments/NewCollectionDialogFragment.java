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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.android.common.validation.ValidatorHelper;
import eu.trentorise.smartcampus.eb.HomeActivity;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.Utils;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.UserPreference;

public class NewCollectionDialogFragment extends SherlockDialogFragment {

	private static final String ARG_COLL = "coll";
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return saveCollectionDialog(getActivity());
	}

	public interface CollectionSavedHandler {
		void onCollectionSaved(ExpCollection coll);
	}

	public static Bundle prepare(ExpCollection coll) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_COLL, coll);
		return b;
	}
	
	private Dialog saveCollectionDialog(final Activity ctx) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		final ExpCollection coll = getArguments() == null ? null : (ExpCollection)getArguments().getSerializable(ARG_COLL);
		
		if (coll != null) {
			builder.setTitle(R.string.dialog_collection_edit);
		} else {
			builder.setTitle(R.string.dialog_collection_add);
		}
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.newcollectiondialog, null);
		final EditText newCollectionEditText = (EditText) v.findViewById(R.id.newCollectionEditText);
		if (coll != null) {
			newCollectionEditText.setText(coll.getName());
		}
		builder.setPositiveButton(coll == null ? R.string.dialog_create : R.string.dialog_save, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.setView(v);
		final AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(ctx);

		OnClickListener onSaveBtnListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				UserPreference uup = EBHelper.getUserPreference();
				List<ExpCollection> tmpList = uup.getCollections();
				ExpCollection updated = null;
				
				if (newCollectionEditText.getText() == null || newCollectionEditText.getText().toString().trim().equals("")) {
					ValidatorHelper.highlight(getActivity(), newCollectionEditText, getString(R.string.alert_folder_name_required));
					return;
				}
				
				if (coll != null) {
					for (ExpCollection ec : tmpList) {
						if (ec.getId().equals(coll.getId())) {
							ec.setName(newCollectionEditText.getText().toString());
							updated = ec;
							break;
						}
					}
				} else {
					ExpCollection newCollection = new ExpCollection(Utils.generateUID(), newCollectionEditText.getText().toString());
					tmpList.add(newCollection);
					updated = newCollection;
				}
				uup.setCollections(tmpList);
				boolean success = EBHelper.updateUserPreference(ctx, uup);
				if (success) {
					//CollectionSavedHandler c = ((DialogCallbackContainer)getActivity()).getCollectionSavedHandler();
//					if (c != null) {
//						c.onCollectionSaved(updated);
//					}
//					Fragment f = getFragmentManager().findFragmentById(getArguments().getInt(ARG_CONTAINER_ID));
//					if (f instanceof CollectionSavedHandler) {
//						((CollectionSavedHandler)f).onCollectionSaved(updated);
//					}
					if(ctx instanceof HomeActivity){
						((HomeActivity)ctx).refreshMenuList();
					}
					dialog.dismiss();
				}
			}
		};
		dialog.show();
		EBHelper.applyScaleAnimationOnView(newCollectionEditText);
		EBHelper.openKeyboard(getActivity(), newCollectionEditText);
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(onSaveBtnListener);
		return dialog;
	}
}
