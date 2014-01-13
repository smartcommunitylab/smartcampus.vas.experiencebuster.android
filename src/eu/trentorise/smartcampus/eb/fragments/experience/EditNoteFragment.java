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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;

public class EditNoteFragment extends SherlockDialogFragment {

	public interface NoteHandler {
		void onNote(String note, int idx);
	}

	private static final String ARG_NOTE = "note";
	private static final String ARG_IDX = "index";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String text = null;
		if (getArguments() != null) {
			text = getArguments().getString(ARG_NOTE);
		} else if (savedInstanceState != null) {
			text = savedInstanceState.getString(ARG_NOTE);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.exp_notes_title);
		final View v = LayoutInflater.from(getActivity()).inflate(R.layout.edit_exp_note, null);
		builder.setView(v);
		final EditText et = (EditText)v.findViewById(R.id.exp_note_dialog_note);
		if (text != null) {

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
				NoteHandler c = ((DialogCallbackContainer)getActivity()).getNoteHandler();
				c.onNote(et.getText() != null ? et.getText().toString() : null, getArguments().getInt(ARG_IDX));
//
//				((NoteHandler)getFragmentManager().findFragmentById(getArguments().getInt(ARG_CONTAINER_ID))).onNote(et.getText() != null ? et.getText().toString() : null, getArguments().getInt(ARG_IDX));
				dialog.dismiss();
			}
		});
		return builder.create();	
	}

	public static Bundle prepare(String note, int i) {
		Bundle b = new Bundle();
		b.putString(ARG_NOTE, note);
		b.putInt(ARG_IDX, i);
		return b;
	}

}
