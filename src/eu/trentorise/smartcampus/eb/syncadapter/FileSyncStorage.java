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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.filestorage.client.model.Metadata;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.AndroidFilestorage;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.DataException;
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

	private AndroidFilestorage filestorage;

	private static Map<String, BasicObject> expToDelete = new ConcurrentHashMap<String, BasicObject>();
	private static List<String> contentToDelete = new ArrayList<String>();
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
			filestorage = new AndroidFilestorage(GlobalConfig.getAppUrl(context
					.getApplicationContext()) + Constants.FILE_SERVICE,
					Constants.APP_NAME);
		} catch (ProtocolException e) {
			e.printStackTrace();
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

	public void removeContent(Content c) {
		if (c != null
				&& (c.getValue() != null || fileStoraging.containsKey(c
						.getLocalValue()))) {
			String resourceId = c.getValue() != null ? c.getValue()
					: fileStoraging.get(c.getLocalValue());
			contentToDelete.add(resourceId);
		}
	}

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

	public SyncData synchroFile(String authToken, String host, String service)
			throws StorageConfigurationException, SecurityException,
			ConnectionException, DataException, ProtocolException {
		SyncData syncData = helper.getDataToSync(getSyncVersion());
		synchroFile(syncData, authToken);
		return super.synchronize(authToken, host, service);
	}

	private boolean synchroFile(SyncData data, String authToken) {

		// save new resources
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
								String userAccountId = EBHelper
										.getConfiguration(
												EBHelper.CONF_USER_ACCOUNT,
												String.class);
								if (userAccountId != null
										&& EBHelper
												.checkFileSizeConstraints(res)) {
									Metadata meta = filestorage
											.storeResourceByUser(
													res.getContent(),
													res.getName(),
													res.getContentType(),
													authToken, userAccountId,
													false);
									fileStoraging.put(c.getLocalValue(),
											meta.getResourceId());
									c.setValue(meta.getResourceId());
								}
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

		// delete removed contents
		Iterator<String> iter = contentToDelete.iterator();
		while (iter.hasNext()) {
			try {
				filestorage.deleteResourceByUser(authToken, iter.next());
				iter.remove();
			} catch (Exception e) {
				Log.e(getClass().getName(), "Exception deleting file content");
			}
		}

		// delete contents of deleted experiences
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
									filestorage.deleteResourceByUser(authToken,
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

	public static Map<String, BasicObject> getExpToDelete() {
		return expToDelete;
	}

	public static void setExpToDelete(Map<String, BasicObject> expToDelete) {
		FileSyncStorage.expToDelete = expToDelete;
	}

}
