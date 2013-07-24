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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.eb.CatchActivity;
import eu.trentorise.smartcampus.eb.Constants.CATCH_TYPES;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.model.Experience;

public class GrabDialogFragment extends SherlockDialogFragment {

	public static final String ARG_EXP = "exp";
	
	private Experience exp;
	
	static GrabDialogFragment newInstance() {
		GrabDialogFragment f = new GrabDialogFragment();
		return f;
	}

	public GrabDialogFragment() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_EXP)) {
			exp = (Experience) savedInstanceState.getSerializable(ARG_EXP);
		} else if (getArguments() != null && getArguments().containsKey(ARG_EXP)) {
			exp = (Experience) getArguments().getSerializable(ARG_EXP);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_grab);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.grabdialog, null);
		ListView list = (ListView) v.findViewById(R.id.grabList);
		final String[] items = getResources().getStringArray(R.array.grabDialogArray);
		final String[] types = getResources().getStringArray(R.array.grabDialogTypesArray);
		final String[] labels = getResources().getStringArray(R.array.grabDialogLabelsArray);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,
				items);
		list.setAdapter(arrayAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String typeString = types[position];
				final String[] elems = typeString.split("\\|");
				final String[] labelElems = labels[position].split("\\|");
				assert labelElems.length == elems.length;
				final Intent i = new Intent(getActivity(), CatchActivity.class);
				if (elems.length > 1) {
					getDialog().hide();
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setItems(labelElems, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startCapture(i, elems[which]);
							dialog.dismiss();
							dismiss();
						}
					});
					AlertDialog dlg = builder.create();
					dlg.setOwnerActivity(getActivity());
					dlg.show();
				} else {
					startCapture(i, typeString);
					dismiss();
				}
			}

		});

		builder.setView(v);
		AlertDialog dlg = builder.create();
		dlg.setOwnerActivity(getActivity());
		return dlg;
	}

	private void startCapture(Intent i, String typeString) {
		CATCH_TYPES type = CATCH_TYPES.valueOf(typeString);
		i.putExtra(CatchActivity.ARG_TYPE, type.toString());
		i.putExtra(CatchActivity.ARG_EXP, exp);
		startActivity(i);
	}

}
