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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.Filestorage;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.SyncData;
import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;

/**
 * Extension of SyncStorage to manage file synchronization
 * 
 * @author mirko perillo
 * 
 */
public class FileSyncStorage extends SyncStorageWithPaging {

	private Filestorage filestorage;

	private static final String USER_ACCOUNT_ID = "";

	private static Map<String, BasicObject> expToDelete = new ConcurrentHashMap<String, BasicObject>();
	private static Map<String, String> fileStoraging = new ConcurrentHashMap<String, String>();

	/**
	 * @param context
	 * @param appToken
	 * @param dbName
	 * @param dbVersion
	 * @param config
	 * @throws ProtocolException
	 */
	public FileSyncStorage(Context context, String appToken, String dbName,
			int dbVersion, StorageConfiguration config) {
		super(context, appToken, dbName, dbVersion, config);

		try {
			filestorage = new Filestorage(context, Constants.APP_NAME,
					appToken, GlobalConfig.getAppUrl(mContext),
					Constants.FILE_SERVICE);
		} catch (ProtocolException e) {
			Log.e(getClass().getName(), e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.trentorise.smartcampus.storage.sync.SyncStorage#delete(java.lang.String
	 * , java.lang.Class)
	 */
	@Override
	public void delete(String id, Class<? extends BasicObject> cls)
			throws DataException, StorageConfigurationException {

		BasicObject o = getObjectById(id, cls);
		expToDelete.put(id, o);

		super.delete(id, cls);
	}

	public <T extends BasicObject> void update(T input, boolean upsert)
			throws DataException, StorageConfigurationException {
		updateContent(input);
		super.update(input, upsert);
	}

	public <T extends BasicObject> void update(T input, boolean upsert,
			boolean sync) throws DataException, StorageConfigurationException {
		updateContent(input);
		super.update(input, upsert, sync);
	};

	private <T extends BasicObject> void updateContent(T input) {
		if (input instanceof Experience) {
			Experience exp = (Experience) input;
			for (Content c : exp.getContents()) {
				if ((c.getValue() == null || c.getValue().length() == 0)
						&& fileStoraging.containsKey(c.getLocalValue())) {
					c.setValue(fileStoraging.get(c.getLocalValue()));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging#query(java
	 * .lang.Class, java.lang.String, java.lang.String[], int, int)
	 */
	@Override
	public <T extends BasicObject> Collection<T> query(Class<T> cls,
			String selection, String[] args, int offset, int limit)
			throws DataException, StorageConfigurationException {
		Collection<T> result = super.query(cls, selection, args, offset, limit);
		if (cls == Experience.class) {
			Collection<Experience> exps = (Collection<Experience>) result;
			for (Experience exp : exps) {
				for (Content c : exp.getContents()) {
					if (!new File(c.getLocalValue()).exists()
							&& c.getValue() != null) {
						new FileloaderExecutor().execute(c.getValue(),
								c.getLocalValue());
					}
				}
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging#query(java
	 * .lang.Class, java.lang.String, java.lang.String[], int, int,
	 * java.lang.String)
	 */
	@Override
	public <T extends BasicObject> Collection<T> query(Class<T> cls,
			String selection, String[] args, int offset, int limit,
			String orderBy) throws DataException, StorageConfigurationException {
		Collection<T> result = super.query(cls, selection, args, offset, limit,
				orderBy);

		return result;
	}

	private <T extends BasicObject> void loadRemoteFiles(Class<T> cls,
			Collection<T> result) {
		if (cls == Experience.class) {
			Collection<Experience> exps = (Collection<Experience>) result;
			for (Experience exp : exps) {
				for (Content c : exp.getContents()) {
					if (!new File(c.getLocalValue()).exists()
							&& c.getValue() != null) {
						new FileloaderExecutor().execute(c.getValue(),
								c.getLocalValue());
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.trentorise.smartcampus.storage.sync.SyncStorage#query(java.lang.Class,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public <T extends BasicObject> Collection<T> query(Class<T> cls,
			String selection, String[] args) throws DataException,
			StorageConfigurationException {
		// TODO Auto-generated method stub
		return super.query(cls, selection, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.trentorise.smartcampus.storage.sync.SyncStorage#synchronize(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	// @Override
	// public SyncData synchronize(String authToken, String host, String
	// service)
	// throws SecurityException, ConnectionException, DataException,
	// ProtocolException, StorageConfigurationException {
	// SyncData syncData = helper.getDataToSync(getSyncVersion());
	// Intent intent = new Intent(mContext, FileSyncService.class);
	// intent.putExtra("syncData", new FileSyncData(syncData));
	// mContext.startService(intent);
	// synchroFile(syncData, authToken);
	// return super.synchronize(authToken, host, service);
	// }

	public SyncData synchroFile(String authToken, String host, String service)
			throws StorageConfigurationException, SecurityException,
			ConnectionException, DataException, ProtocolException {
		SyncData syncData = helper.getDataToSync(getSyncVersion());
		// Intent intent = new Intent(mContext, FileSyncService.class);
		// FileSyncData fileSyncData = new FileSyncData();
		// fileSyncData.setDeleted(syncData.getDeleted());
		// fileSyncData.setUpdated(syncData.getUpdated());
		// fileSyncData.setVersion(syncData.getVersion());
		// intent.putExtra("syncData", fileSyncData);
		// mContext.startService(intent);
		synchroFile(syncData, authToken);
		// new FilestorageExecutor(syncData).execute();
		return super.synchronize(authToken, host, service);
	}

	private boolean synchroFile(SyncData data, String authToken) {
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
							if ((c.getValue() == null || c.getValue().length() == 0)
									&& !fileStoraging.containsKey(c
											.getLocalValue())) {
								Resource res = eu.trentorise.smartcampus.eb.custom.Utils
										.getResource(mContext,
												c.getLocalValue());
								String rid = filestorage.storeResource(
										res.getContent(), res.getContentType(),
										res.getName(), authToken,
										USER_ACCOUNT_ID);
								fileStoraging.put(c.getLocalValue(), rid);
								c.setValue(rid);
							}

							if ((c.getValue() == null || c.getValue().length() == 0)
									&& fileStoraging.containsKey(c
											.getLocalValue())) {
								c.setValue(fileStoraging.get(c.getLocalValue()));
							}
						} catch (Exception e) {
							Log.e(getClass().getName(),
									"Exception storing file content");
						}
					}
				}
				EBHelper.saveExperience(null, exp, false);
			}
		}

		if (data.getDeleted().get(
				"eu.trentorise.smartcampus.eb.model.Experience") != null) {
			for (Object o : data.getDeleted().get(
					"eu.trentorise.smartcampus.eb.model.Experience")) {
				Experience exp = (Experience) expToDelete.get(o);
				expToDelete.remove(o);
				try {
					for (Content c : exp.getContents()) {
						if (c.isStorable()) {
							if (c.getValue() != null
									&& c.getValue().length() > 0) {
								try {
									filestorage.deleteResource(authToken,
											USER_ACCOUNT_ID, c.getValue());
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

	public static Map<String, BasicObject> getExpToDelete() {
		return expToDelete;
	}

	public static void setExpToDelete(Map<String, BasicObject> expToDelete) {
		FileSyncStorage.expToDelete = expToDelete;
	}

	class FilestorageExecutor extends AsyncTask<Void, Void, Void> {

		private SyncData data;

		public FilestorageExecutor(SyncData data) {
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

	class FileloaderExecutor extends AsyncTask<String, Void, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(String... params) {
			String id = params[0];
			String path = params[1];
			try {
				eu.trentorise.smartcampus.storage.model.Resource r = filestorage
						.getResource(EBHelper.getAuthToken(), id);
				FileOutputStream fout = new FileOutputStream(path);
				fout.write(r.getContent());
				fout.close();
			} catch (ClientProtocolException e) {
			} catch (ProtocolException e) {
			} catch (ConnectionException e) {
			} catch (SecurityException e) {
			} catch (IOException e) {
				Log.e(getClass().getName(), e.getMessage());
			}
			return null;
		}

	}

}
