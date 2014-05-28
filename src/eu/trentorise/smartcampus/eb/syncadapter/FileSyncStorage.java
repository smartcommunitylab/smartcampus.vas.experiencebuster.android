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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.Utils;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Metadata;
import eu.trentorise.smartcampus.filestorage.client.model.StorageType;
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

	private static final String TAG = "FileSyncStorage";

	private AndroidFilestorage filestorage;
	private FileSyncDatasource fileToSync;

	private static Map<String, BasicObject> expToDelete = new ConcurrentHashMap<String, BasicObject>();
	private static List<String> contentToDelete = new ArrayList<String>();

	private Context ctx;

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
		ctx = context;
		fileToSync = new FileSyncDatasource(context);
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

		if (input instanceof Experience) {
			Experience update = (Experience) input;
			Experience saved = EBHelper.findLocalExperienceById(input.getId());
			if (saved != null) {
				List<Content> updateContents = update.getContents();
				for (Content content : saved.getContents()) {
					int index = updateContents.indexOf(content);
					if (index > -1 && content.getValue() != null) {
						updateContents.get(index).setValue(content.getValue());
					}
				}
			}
		}
		super.update(input, upsert);
	}

	public <T extends BasicObject> void update(T input, boolean upsert,
			boolean sync) throws DataException, StorageConfigurationException {
		if (input instanceof Experience) {
			Experience update = (Experience) input;
			Experience saved = EBHelper.findLocalExperienceById(input.getId());
			if (saved != null) {
				List<Content> updateContents = update.getContents();
				for (Content content : saved.getContents()) {
					int index = updateContents.indexOf(content);
					if (index > -1 && content.getValue() != null) {
						updateContents.get(index).setValue(content.getValue());
					}
				}
			}
		}
		super.update(input, upsert, sync);
	};

	public void removeContent(Content c) {
		contentToDelete.add(c.getValue());
	}

	public synchronized SyncData synchroFile(String authToken, String host,
			String service) throws StorageConfigurationException,
			SecurityException, ConnectionException, DataException,
			ProtocolException {
		SyncData syncData = helper.getDataToSync(getSyncVersion());
		try {
			synchroFile(syncData, authToken);
		} catch (FilestorageException e) {
			throw new ProtocolException(e.getMessage());
		}
		return synchronize(authToken, host, service);
	}

	public void syncFiles() {
		List<SyncFile> syncFiles = fileToSync.getEntryToProcess();
		long totalUploadedSize = Constants.FILE_SYNC_UPLOAD_SIZE;
		boolean forceSynchro = false, resetEntry = false, contentExist = false;
		for (SyncFile syncFile : syncFiles) {
			forceSynchro = resetEntry = contentExist = false;
			try {
				if (syncFile.getTentative() < Constants.FILE_SYNC_MAX_TENTATIVES) {
					Resource res = eu.trentorise.smartcampus.eb.custom.Utils
							.getResource(mContext, syncFile.getPath());
					if (res != null) {
						String userAccountId = EBHelper.getConfiguration(
								EBHelper.CONF_USER_ACCOUNT, String.class);
						if (userAccountId != null) {
							eu.trentorise.smartcampus.filestorage.client.model.Account account = filestorage
									.getAccountByUser(EBHelper.getAuthToken());
							Experience exp = EBHelper
									.findLocalExperienceById(syncFile
											.getIdExp());
							if (exp != null) {
								for (Content c : exp.getContents()) {
									if (c.isStorable()
											&& c.getId().equals(
													syncFile.getIdContent())) {
										contentExist = true;

										Metadata meta = null;
										if (account.getStorageType() == StorageType.DROPBOX) {
											meta = filestorage.storeOnDropbox(
													res.getResourcefile(),
													EBHelper.getAuthToken(),
													userAccountId, false, ctx);
											Log.i(TAG, "Stored on dropbox");
										} else {
											meta = filestorage
													.storeResourceByUser(
															res.getResourcefile(),
															new FileInputStream(
																	res.getResourcefile()),
															EBHelper.getAuthToken(),
															userAccountId, true);
											Log.i(TAG,
													"Stored through fs service");
										}
										c.setValue(meta.getResourceId());
										Log.i(TAG,
												String.format(
														"Setted value %s for content %s",
														meta.getResourceId(),
														c.getId()));
										if (EBHelper.saveExperience(null, exp,
												false) == null) {
											fileToSync
													.updateStatus(
															syncFile,
															FileSyncDbHelper.ST_FAIL_DB);
										} else {
											forceSynchro = true;
											fileToSync.removeEntry(syncFile
													.getIdEntry());
										}

									}
								}
								resetEntry = !contentExist;
							} else {
								resetEntry = true;
							}
							if (resetEntry) {
								fileToSync.removeEntry(syncFile.getIdEntry());
							}

							// check total uploaded size
							if ((totalUploadedSize -= res.getSize()) < 0) {
								Log.i(TAG, "Max uploaded size reached: "
										+ Constants.FILE_SYNC_UPLOAD_SIZE);
								break;
							}

						}
					} else {
						Log.i(TAG,
								String.format(
										"Exp %s, content %s problem loading local resource %s",
										syncFile.getIdExp(),
										syncFile.getIdContent(),
										syncFile.getPath()));
						fileToSync.updateStatus(syncFile,
								FileSyncDbHelper.ST_FAIL_RESOURCE);
					}
				} else {
					Log.i(TAG,
							String.format(
									"Exp %s, content %s reachs max tentative %d of upload",
									syncFile.getIdExp(),
									syncFile.getIdContent(),
									syncFile.getTentative()));
					fileToSync.removeEntry(syncFile.getIdEntry());
				}
			} catch (DataException e) {
				Log.e(TAG,
						String.format(
								"Error getting configuration, synchronizing exp %s content %s",
								syncFile.getIdExp(), syncFile.getIdContent()));
				fileToSync.updateStatus(syncFile,
						FileSyncDbHelper.ST_FAIL_SERVICE);
			} catch (AACException e) {
				Log.e(TAG, String.format(
						"Authentication error synchronizing exp %s content %s",
						syncFile.getIdExp(), syncFile.getIdContent()));
				fileToSync.updateStatus(syncFile,
						FileSyncDbHelper.ST_FAIL_SERVICE);
			} catch (Exception e) {
				Log.e(TAG, String.format(
						"General error synchronizing exp %s content %s",
						syncFile.getIdExp(), syncFile.getIdContent()));
				fileToSync.updateStatus(syncFile,
						FileSyncDbHelper.ST_FAIL_SERVICE);
			}

		}

		if (forceSynchro) {
			try {
				synchronize(
						EBHelper.getAuthToken(),
						GlobalConfig.getAppUrl(mContext),
						eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_SERVICE);
			} catch (Exception e) {
				Log.e(TAG, "exception synchronizing update fid");
			}
		}
	}

	private boolean synchroFile(SyncData data, String authToken)
			throws DataException, FilestorageException {

		// save new resources
		if (data.getUpdated().get(Experience.class.getCanonicalName()) != null) {
			for (Object o : data.getUpdated().get(
					Experience.class.getCanonicalName())) {
				Experience exp = eu.trentorise.smartcampus.android.common.Utils
						.convertObjectToData(Experience.class, o);
				for (Content c : exp.getContents()) {
					if (c.isStorable()) {

						if (!c.isUploaded()) {
							if (EBHelper.checkFileSizeConstraints(Utils
									.getResource(ctx, c.getLocalValue()))) {
								fileToSync.insertEntry(exp.getId(), c.getId(),
										c.getLocalValue());
							} else {
								Log.i(TAG, String.format(
										"Content %s size greater than %s MB", c
												.getLocalValue(),
										EBHelper.getConfiguration(
												EBHelper.CONF_FILE_SIZE,
												String.class)));
							}
						}
					}
				}
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
