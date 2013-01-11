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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.eb.R;

public class EditPositionFragment extends SherlockDialogFragment {

	public interface PositionHandler {
		void onPosition(String text);
	}

	private static final String ARG_TXT = "text";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String text = null;
		if (getArguments() != null) {
			text = getArguments().getString(ARG_TXT);
		} else if (savedInstanceState != null) {
			text = savedInstanceState.getString(ARG_TXT);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.exp_position_title);
		final View v = LayoutInflater.from(getActivity()).inflate(R.layout.edit_exp_position, null);
		builder.setView(v);

		if (text != null) {
			EditText et = (EditText)v.findViewById(R.id.exp_position_dialog_position);
			et.setText(text);
		}
		

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText et = (EditText)v.findViewById(R.id.exp_position_dialog_position);
				PositionHandler ph = ((DialogCallbackContainer)getActivity()).getPositionHandler();
				ph.onPosition(et.getText() != null ? et.getText().toString() : null);
				dialog.dismiss();
			}
		});

		return builder.create();	
	}

	public static Bundle prepare(String text) {
		Bundle b = new Bundle();
		b.putString(ARG_TXT, text);
		return b;
	}

}
