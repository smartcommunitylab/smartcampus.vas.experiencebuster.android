/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.filestorage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.filestorage.client.model.Account;
import eu.trentorise.smartcampus.filestorage.client.model.StorageType;
import eu.trentorise.smartcampus.storage.AndroidFilestorage;

public class FilestorageAccountActivity extends Activity {

	private static final int AUTH_REQUESTCODE = 100;

	public static final String EXTRA_USER_ACCOUNT_ID = "USER_ACCOUNT_ID";

	public static final String EXTRA_SHOW_DIALOG = "SHOW_DIALOG";

	/** Logging tag */
	private static final String TAG = "File";

	private Account account = null;

	/** Access token for the application user */
	// private String mToken = null;
	/** Filestorage connector reference */
	// private Filestorage mFilestorage = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		if (i.getBooleanExtra(EXTRA_SHOW_DIALOG, true)) {
			new AlertDialog.Builder(this)
					.setTitle("")
					.setMessage(
							eu.trentorise.smartcampus.eb.R.string.msg_synchro_dialog)
					.setPositiveButton(
							eu.trentorise.smartcampus.eb.R.string.synchro_dialog_y,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									new AccountTask().execute();
									// new AppAccountTask().execute();
									// try {
									// mFilestorage
									// .startAuthActivityForResult(
									// FilestorageAccountActivity.this,
									// EBHelper.getAuthToken(),
									// StorageType.DROPBOX,
									// AUTH_REQUESTCODE);
									// } catch (AACException e) {
									// e.printStackTrace();
									// }
								}
							})
					.setNegativeButton(
							eu.trentorise.smartcampus.eb.R.string.synchro_dialog_n,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									setResult(Activity.RESULT_CANCELED);
									finish();
								}
							}).setCancelable(false).show();
		} else {
			// new AppAccountTask().execute();
			new AccountTask().execute();
			// try {
			// AndroidFilestorage mFilestorage = new AndroidFilestorage(
			// Constants.FILE_SERVICE, Constants.APP_NAME);
			// mFilestorage.startAuthActivityForResult(
			// FilestorageAccountActivity.this,
			// EBHelper.getAuthToken(), StorageType.DROPBOX,
			// AUTH_REQUESTCODE);
			// } catch (AACException e) {
			// e.printStackTrace();
			// }
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// user account acquisition complete
		if (requestCode == AUTH_REQUESTCODE) {
			// user account acquired
			if (resultCode == Activity.RESULT_OK) {
				String accountId = data
						.getStringExtra(AndroidFilestorage.EXTRA_OUTPUT_ACCOUNT_ID);

				Intent result = new Intent();
				result.putExtra(EXTRA_USER_ACCOUNT_ID, accountId);
				setResult(Activity.RESULT_OK, result);
				finish();

				// user account cancelled
			} else if (resultCode == Activity.RESULT_CANCELED) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}
	}

	class AccountTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				AndroidFilestorage mFilestorage = new AndroidFilestorage(
						Constants.FILE_SERVICE, Constants.APP_NAME);
				mFilestorage.startAuthActivityForResult(
						FilestorageAccountActivity.this,
						EBHelper.getAuthToken(), StorageType.DROPBOX,
						AUTH_REQUESTCODE);
			} catch (AACException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	/*
	 * class AppAccountTask extends AsyncTask<Void, Void, List<Storage>> {
	 * 
	 * private AndroidFilestorage mFilestorage;
	 * 
	 * public AppAccountTask() { mFilestorage = new
	 * AndroidFilestorage(Constants.FILE_SERVICE, Constants.APP_NAME); }
	 * 
	 * @Override protected List<Storage> doInBackground(Void... params) { try {
	 * // read app accounts return mFilestorage.gets } catch (Exception e) {
	 * e.printStackTrace(); return null; } }
	 * 
	 * @Override protected void onPostExecute(List<AppAccount> result) { //
	 * request new account for the required app if (result != null &&
	 * result.size() > 0) { AppAccount appAccount = result.get(0); try {
	 * mFilestorage.startAuthActivityForResult( FilestorageAccountActivity.this,
	 * EBHelper.getAuthToken(), appAccount.getAppAccountName(),
	 * appAccount.getId(), StorageType.DROPBOX, AUTH_REQUESTCODE); } catch
	 * (AACException e) { e.printStackTrace(); } } else { Toast.makeText(
	 * FilestorageAccountActivity.this,
	 * eu.trentorise.smartcampus.eb.R.string.msg_synchro_no_appaccount,
	 * Toast.LENGTH_LONG).show(); finish(); } } }
	 */
}
