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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.storage.Filestorage;
import eu.trentorise.smartcampus.storage.model.AppAccount;
import eu.trentorise.smartcampus.storage.model.StorageType;
import eu.trentorise.smartcampus.storage.model.UserAccount;

public class FilestorageAccountActivity extends Activity {

	private static final int AUTH_REQUESTCODE = 100;

	/** Logging tag */
	private static final String TAG = "File";

	private UserAccount userAccount = null;

	/** Access token for the application user */
	private String mToken = null;
	/** Filestorage connector reference */
	private Filestorage mFilestorage = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mToken = EBHelper.getAuthToken();
		try {
			mFilestorage = new Filestorage(this, Constants.APP_NAME,
					Constants.APP_TOKEN,
					GlobalConfig.getAppUrl(getApplicationContext()),
					Constants.FILE_SERVICE);
		} catch (ProtocolException e1) {
			Log.e(TAG, "problem getting filestorage application url");
		}

		new AppAccountTask().execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// user account acquisition complete
		if (requestCode == AUTH_REQUESTCODE) {
			// user account acquired
			if (resultCode == Activity.RESULT_OK) {
				userAccount = data
						.getParcelableExtra(Filestorage.EXTRA_OUTPUT_USERACCOUNT);

				Intent result = new Intent();
				result.putExtra("USER_ACCOUNT_ID", userAccount.getId());
				setResult(Activity.RESULT_OK, result);
				finish();

				// user account cancelled
			} else if (resultCode == Activity.RESULT_CANCELED) {
				setResult(Activity.RESULT_CANCELED);
				Toast.makeText(this, "CANCELLED", Toast.LENGTH_LONG).show();
				// user account failed
			} else {
				Toast.makeText(this, "ERROR: " + resultCode, Toast.LENGTH_LONG)
						.show();
			}
		}
		// super.onActivityResult(requestCode, resultCode, data);
	}

	class AppAccountTask extends AsyncTask<Void, Void, List<AppAccount>> {

		@Override
		protected List<AppAccount> doInBackground(Void... params) {
			try {
				// read app accounts
				return mFilestorage.getAppAccounts(mToken);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<AppAccount> result) {
			// request new account for the required app
			if (result != null && result.size() > 0) {
				AppAccount appAccount = result.get(0);
				mFilestorage.startAuthActivityForResult(
						FilestorageAccountActivity.this, mToken,
						appAccount.getAppAccountName(), appAccount.getId(),
						StorageType.DROPBOX, AUTH_REQUESTCODE);
			} else {
				Toast.makeText(FilestorageAccountActivity.this, "No Accounts!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
