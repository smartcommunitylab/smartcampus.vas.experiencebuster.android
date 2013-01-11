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

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class TextEditSwitch {

	private ViewSwitcher switcher ;
	private OnEditListener listener;
	private boolean editMode = false;
	
	public TextEditSwitch(final View mView, int switcherId, int tv, int et, OnEditListener listener) {
		super();
		this.switcher = (ViewSwitcher)mView.findViewById(switcherId);
		this.listener = listener;
		final TextView mTV = (TextView)mView.findViewById(tv);
		mTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editMode = true;
				TextEditSwitch.this.listener.onEdit();
				CharSequence text = ((TextView) v).getText();
				switcher.showNext();
				((EditText) switcher.getCurrentView()).setText(text);
				switcher.getCurrentView().requestFocus();
				InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(switcher.getCurrentView(),0);
			}
		});
		final EditText mET = (EditText) mView.findViewById(et);
		mET.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && editMode) {
					switcher.showPrevious();
					mTV.setText(mET.getText().toString());
				}
			}
		});
		
	}
	
	public void viewMode() {
		editMode = false;
		if (switcher.getCurrentView() instanceof EditText) {
			InputMethodManager imm = (InputMethodManager) switcher.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(switcher.getCurrentView().getWindowToken(), 0);
			CharSequence text = ((EditText) switcher.getCurrentView()).getText();
			switcher.showPrevious();
			((TextView)switcher.getCurrentView()).setText(text == null ? null : text.toString());
			
		}
	}
	public void editMode() {
		if (editMode) return;

		editMode = true;
		TextEditSwitch.this.listener.onEdit();
		CharSequence text = ((TextView) switcher.getCurrentView()).getText();
		switcher.showNext();
		((EditText) switcher.getCurrentView()).setText(text);
		switcher.getCurrentView().requestFocus();
		InputMethodManager imm = (InputMethodManager) switcher.getCurrentView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(switcher.getCurrentView(),0);
	}
	
	public String getValue() {
		if (switcher.getCurrentView() instanceof EditText) {
			Editable e = ((EditText) switcher.getCurrentView()).getText(); 
			if (e != null) return e.toString();
		} else {
			CharSequence cs = ((TextView) switcher.getCurrentView()).getText();
			if (cs != null) return cs.toString();
		}
		return null;
	}

	public void setValue(String v) {
		if (switcher.getCurrentView() instanceof EditText) {
			((EditText) switcher.getCurrentView()).setText(v); 
		} else {
			((TextView) switcher.getCurrentView()).setText(v);
		}
	}
}
