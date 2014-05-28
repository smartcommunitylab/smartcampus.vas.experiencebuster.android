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

import it.smartcampuslab.eb.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.eb.custom.data.EBHelper;

public class DeleteExperienceFragment extends SherlockDialogFragment {

	public interface RemoveCallback {
		void onRemoved(String id);
	}

	private static final String ARG_ID = "experienceId";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.msg_delete_confirm)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String experienceId = getArguments().getString(
										ARG_ID);
								EBHelper.deleteExperience(getActivity(),
										experienceId, true);
								RemoveCallback c = ((DialogCallbackContainer) getActivity())
										.getRemoveCallback();
								c.onRemoved(getArguments().getString(ARG_ID));

								// ((RemoveCallback)getFragmentManager().findFragmentById(getArguments().getInt(ARG_CONTAINER_ID))).onRemoved(experienceId);
							}
						}).setNegativeButton(android.R.string.no, null)
				.create();
	}

	public static Bundle prepare(String id) {
		Bundle b = new Bundle();
		b.putString(ARG_ID, id);
		return b;
	}

}
