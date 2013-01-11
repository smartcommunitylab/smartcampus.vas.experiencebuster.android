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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;

public class TaggingDialogFragment extends SherlockDialogFragment {

	private static final String ARG_INIT = "init";
	private TagProvider tagProvider = new TagProvider() {
		@Override
		public List<SemanticSuggestion> getTags(CharSequence text) {
			try {
				return EBHelper.getSuggestions(text);
			} catch (Exception e) {
				return Collections.emptyList();
			}
		}
	};
	
	private OnTagsSelectedListener tagListener = new OnTagsSelectedListener() {
		
		@Override
		public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
			OnTagsSelectedListener c = ((DialogCallbackContainer)getActivity()).getTagListener();
			c.onTagsSelected(suggestions);
		}
	};
	
	private ArrayList<SemanticSuggestion> initialSuggestions = null;
	
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable(ARG_INIT, initialSuggestions);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			initialSuggestions = (ArrayList<SemanticSuggestion>)savedInstanceState.getSerializable(ARG_INIT);
		} else if (getArguments() != null) {
			initialSuggestions = (ArrayList<SemanticSuggestion>)getArguments().getSerializable(ARG_INIT);
		} else {
			initialSuggestions = new ArrayList<SemanticSuggestion>();
		}
		return new TaggingDialog(getSherlockActivity(), tagListener, tagProvider, initialSuggestions);
	}

	public static Bundle prepare(Collection<SemanticSuggestion> initial) {
		Bundle b = new Bundle();
		if (initial != null) {
			b.putSerializable(ARG_INIT, new ArrayList<SemanticSuggestion>(initial));
		}
		return b;
	}
}
