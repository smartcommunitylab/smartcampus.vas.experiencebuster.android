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
package eu.trentorise.smartcampus.eb.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.storage.Filestorage;

/**
 * @author mirko perillo
 * 
 */
public class FileSyncService extends Service {

	/**
	 * @param name
	 */
	// public FileSyncService(String name) {
	// super(name);
	// }

	public FileSyncService() {
		// super("filesyncservice");
		super();
	}

	private Filestorage filestorage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		try {
			filestorage = new Filestorage(getApplicationContext(),
					Constants.APP_NAME, Constants.APP_TOKEN,
					GlobalConfig.getAppUrl(getApplicationContext()),
					Constants.FILE_SERVICE);
		} catch (ProtocolException e) {
			Log.e(getClass().getName(), e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		FileSyncData data = ((FileSyncData) intent
				.getSerializableExtra("syncData"));
		new FilestorageExecutor(data).execute();
		// synchroFile(data, EBHelper.getAuthToken());
		// EBHelper.synchronize(false);
		return Service.START_NOT_STICKY;
	}

	private boolean synchroFile(FileSyncData data, String authToken) {
		if (data.getUpdated().get(
				"eu.trentorise.smartcampus.eb.model.Experience") != null) {
			for (Object o : data.getUpdated().get(
					"eu.trentorise.smartcampus.eb.model.Experience")) {
				Experience exp = eu.trentorise.smartcampus.android.common.Utils
						.convertObjectToData(Experience.class, o);
				for (Content c : exp.getContents()) {
					if (c.isStorable()) {
						try {
							// replace the file content is not necessary because
							// lifelog doesn't support it
							if (c.getValue() == null
									|| c.getValue().length() == 0) {
								Resource res = eu.trentorise.smartcampus.eb.custom.Utils
										.getResource(getApplicationContext(),
												c.getLocalValue());
								String rid = filestorage.storeResource(
										res.getContent(), res.getContentType(),
										res.getName(), authToken, "");
								c.setValue(rid);
							}
						} catch (Exception e) {
							Log.e(getClass().getName(),
									"Exception storing file content");
						}
					}
				}
				Log.i("FileSYNC", "file sync expid: " + exp.getId());
				EBHelper.saveExperience(null, exp, false);
			}
		}

		if (data.getDeleted().get(
				"eu.trentorise.smartcampus.eb.model.Experience") != null) {
			for (Object o : data.getDeleted().get(
					"eu.trentorise.smartcampus.eb.model.Experience")) {
				Experience exp = (Experience) FileSyncStorage.getExpToDelete()
						.get(o);
				FileSyncStorage.getExpToDelete().remove(o);
				try {
					for (Content c : exp.getContents()) {
						if (c.isStorable()) {
							if (c.getValue() != null
									&& c.getValue().length() > 0) {
								try {
									filestorage.deleteResource(authToken, "",
											c.getValue());
								} catch (Exception e) {
									Log.e(getClass().getName(),
											"Exception deleting file content");
								}
							}
						}
					}
				} catch (Exception e1) {
					Log.e(getClass().getName(),
							"Exception retrieving experience " + (String) o);
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	// @Override
	// protected void onHandleIntent(Intent intent) {
	// FileSyncData data = ((FileSyncData) intent
	// .getSerializableExtra("syncData"));
	// synchroFile(data, EBHelper.getAuthToken());
	// EBHelper.synchronize(false);
	// }

	class FilestorageExecutor extends AsyncTask<Void, Void, Void> {

		private FileSyncData data;

		public FilestorageExecutor(FileSyncData data) {
			this.data = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			synchroFile(data, EBHelper.getAuthToken());
			EBHelper.synchronize(false);
			return null;
		}

	}

}
