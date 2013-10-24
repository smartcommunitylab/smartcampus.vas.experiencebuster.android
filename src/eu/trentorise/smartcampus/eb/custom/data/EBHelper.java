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
package eu.trentorise.smartcampus.eb.custom.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.filestorage.FilestorageAccountActivity;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.eb.model.NearMeObject;
import eu.trentorise.smartcampus.eb.model.ObjectFilter;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.eb.model.UserPreference;
import eu.trentorise.smartcampus.eb.syncadapter.FileSyncStorage;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.remote.RemoteStorage;

public class EBHelper {

	private static final String EB_CONFS = "filelog";

	public static final String CONF_SYNCHRO = "pref_synchro_file";
	public static final String CONF_FILE_SIZE = "pref_max_file_lenght";
	public static final String CONF_USER_ACCOUNT = "EB_USER_ACCOUNT";

	private static final boolean testing = true;

	private static EBHelper instance = null;
	private static RemoteStorage remoteStorage;

	private static SCAccessProvider accessProvider;

	public static Account SCAccount;

	private Context mContext;
	private StorageConfiguration sc = null;
	private FileSyncStorage storage = null;
	private ProtocolCarrier mProtocolCarrier = null;

	private static LocationHelper mLocationHelper;

	private UserPreference preference = null;
	private boolean loaded = false;

	public static void init(Context mContext) throws NameNotFoundException {
		if (instance == null) {
			instance = new EBHelper(mContext);
		}
	}

	public static FileSyncStorage getSyncStorage() throws DataException {
		return getInstance().storage;
	}

	private static RemoteStorage getRemote(Context mContext, String token)
			throws ProtocolException, DataException {
		if (remoteStorage == null) {
			remoteStorage = new RemoteStorage(mContext, Constants.APP_TOKEN);
		}
		remoteStorage.setConfig(token,
				GlobalConfig.getAppUrl(getInstance().mContext),
				Constants.SERVICE);
		return remoteStorage;
	}

	public static Collection<UserPreference> readUserPreference()
			throws DataException, ConnectionException, ProtocolException,
			SecurityException, AACException {
		return getRemote(instance.mContext, getAuthToken()).getObjects(
				UserPreference.class);

	}

	public static synchronized void synchronize(boolean synchronizeFile) {
		if (isSynchronizationActive()) {
			Bundle bundle = new Bundle();
			bundle.putBoolean("synchroFile", synchronizeFile);
			ContentResolver.requestSync(SCAccount,
					"eu.trentorise.smartcampus.eb", bundle);
		}

	}

	public static boolean isSynchronizationActive() {
		try {
			return EBHelper.getConfiguration(CONF_SYNCHRO, Boolean.class);
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(),
					"Error getting synchro configuration. Synchronization is not active!");
			return false;
		}
	}

	public static <T> boolean saveConfiguration(String configuration,
			Object value, Class<T> type) throws DataException {
		SharedPreferences confs = PreferenceManager
				.getDefaultSharedPreferences(getInstance().mContext);

		Editor editor = confs.edit();
		if (type == String.class) {
			editor.putString(configuration, (String) value);
		}
		if (type == Boolean.class) {
			editor.putBoolean(configuration, (Boolean) value);
		}
		return editor.commit();
	}

	public static <T> T getConfiguration(String configuration, Class<T> type)
			throws DataException {
		SharedPreferences confs = PreferenceManager
				.getDefaultSharedPreferences(getInstance().mContext);
		Object o = null;
		if (type == String.class) {
			o = confs.getString(configuration, null);
		}
		if (type == Boolean.class) {
			o = confs.getBoolean(configuration, true);
		}
		return (T) o;
	}

	public static boolean checkFileSizeConstraints(Resource resource) {
		try {
			// express in mb
			float maxSize = Float.valueOf(getConfiguration(CONF_FILE_SIZE,
					String.class));
			// transform in bytes
			maxSize = maxSize * 1048576;
			return resource.getContent().length <= maxSize;
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean checkFileSizeConstraints(long resourceSize) {
		try {
			// express in mb
			float maxSize = Float.valueOf(getConfiguration(CONF_FILE_SIZE,
					String.class));
			// transform in bytes
			maxSize = maxSize * 1048576;
			return resourceSize <= maxSize;
		} catch (Exception e) {
			return true;
		}
	}

	public static void askUserAccount(Activity a, int requestCode,
			boolean showDialog) throws DataException {
		Intent i = new Intent(getInstance().mContext,
				FilestorageAccountActivity.class);
		i.putExtra(FilestorageAccountActivity.EXTRA_SHOW_DIALOG, showDialog);
		a.startActivityForResult(i, requestCode);
	}

	public static void askUserAccount(android.support.v4.app.Fragment f,
			int requestCode, boolean showDialog) throws DataException {
		Intent i = new Intent(getInstance().mContext,
				FilestorageAccountActivity.class);
		i.putExtra(FilestorageAccountActivity.EXTRA_SHOW_DIALOG, showDialog);
		f.startActivityForResult(i, requestCode);
	}

	public static String getAuthToken() throws AACException {
		return getAccessProvider().readToken(instance.mContext);
	}

	private static EBHelper getInstance() throws DataException {
		if (instance == null) {
			throw new DataException("the helper is not initialized");
		}
		return instance;
	}

	public static SCAccessProvider getAccessProvider() {
		return accessProvider;
	}

	protected EBHelper(Context mContext) throws NameNotFoundException {
		super();
		this.mContext = mContext;
		this.sc = new EBStorageConfiguration();
		this.storage = new FileSyncStorage(
				mContext,
				eu.trentorise.smartcampus.eb.custom.data.Constants.APP_TOKEN,
				eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_DB_NAME,
				1, sc);
		this.mProtocolCarrier = new ProtocolCarrier(mContext,
				eu.trentorise.smartcampus.eb.custom.data.Constants.APP_TOKEN);

		SCAccount = new Account(
				eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
				eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));

		accessProvider = SCAccessProvider.getInstance(mContext);

		setLocationHelper(new LocationHelper(mContext));
	}

	public static void start() throws RemoteException, DataException,
			StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException, AACException {
		// UserPreference
		Collection<UserPreference> userPreferencesCollection = getInstance().storage
				.getObjects(UserPreference.class);
		if (userPreferencesCollection.isEmpty()) {
			userPreferencesCollection = readUserPreference();
		}
		// if not in remotestorage to
		if (userPreferencesCollection.isEmpty()) {
			UserPreference userPreference = new UserPreference();
			userPreference.setSocialUserId(1L);
			getInstance().preference = getInstance().storage
					.create(userPreference);
		}
		getInstance().preference = userPreferencesCollection.iterator().next();
		synchronize(true);
	}

	public static void endAppFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id),
				Toast.LENGTH_LONG).show();
		activity.finish();
	}

	public static void showFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id),
				Toast.LENGTH_LONG).show();
	}

	public static UserPreference getUserPreference() {
		try {
			return getInstance().preference;
		} catch (DataException e) {
			Log.e(EBHelper.class.getName(), "" + e.getMessage());
			return new UserPreference();
		}
	}

	public static boolean updateUserPreference(Activity a,
			UserPreference userPreference) {
		try {
			getInstance().preference = userPreference;
			getInstance().storage.update(getInstance().preference, false);
			synchronize(true);
			return true;
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(),
					"Failed to store preferences: " + e.getMessage());
			showFailure(a, R.string.error_collection_store);
			return false;
		}
	}

	public static List<SemanticSuggestion> getSuggestions(CharSequence suggest)
			throws ConnectionException, ProtocolException, SecurityException,
			DataException, AACException {
		return SuggestionHelper.getSuggestions(suggest, getInstance().mContext,
				GlobalConfig.getAppUrl(getInstance().mContext), getAuthToken(),
				eu.trentorise.smartcampus.eb.custom.data.Constants.APP_TOKEN);
	}

	public static List<Experience> getExperiences(int position, int size) {
		return findExperiences(new ExperienceFilter(), position, size);
	}

	public static List<Experience> findExperiences(
			ExperienceFilter experienceFilter, int position, int size) {
		String query = "";
		List<String> params = new ArrayList<String>();

		assert experienceFilter != null;
		if (experienceFilter.getCollectionId() != null) {
			query += (query.length() > 0 ? " AND " : "")
					+ "collectionIds LIKE '%\""
					+ experienceFilter.getCollectionId() + "\"%'";
		}

		if (experienceFilter.getText() != null
				&& experienceFilter.getText().length() > 0) {
			query += (query.length() > 0 ? " AND " : "") + "experience MATCH ?";
			params.add(experienceFilter.getText());
		}

		Collection<Experience> collection = null;
		try {
			collection = getInstance().storage.query(Experience.class, query,
					params.toArray(new String[params.size()]), position, size,
					"creationTime DESC");
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(), "" + e.getMessage());
			return Collections.emptyList();
		}

		if (collection.size() > 0)
			return new ArrayList<Experience>(collection);
		return Collections.emptyList();
	}

	public static Experience saveExperience(Activity a, Experience exp,
			boolean synchronize) {
		try {
			Experience res = null;
			if (exp.getId() == null) {
				res = getInstance().storage.create(exp);
			} else {
				getInstance().storage.update(exp, false);
				res = exp;
			}
			if (synchronize) {
				synchronize(true);
			}
			return res;
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(),
					"Failed to store experience: " + e.getMessage());
			if (a != null) {
				showFailure(a, R.string.error_exp_store);
			}
			return exp;
		}
	}

	public static void deleteExperience(Activity a, String id,
			boolean synchronize) {
		try {
			getInstance().storage.delete(id, Experience.class);
			if (synchronize) {
				synchronize(true);
			}
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(),
					"Failed to delete experience: " + e.getMessage());
			if (a != null) {
				showFailure(a, R.string.error_exp_store);
			}
		}
	}

	public static List<NearMeObject> getNearMeNowSuggestions(double[] location,
			long currentTimeMillis, boolean filterEvents,
			boolean filterLocations) throws Exception {
		MessageRequest request = new MessageRequest(
				GlobalConfig.getAppUrl(getInstance().mContext),
				eu.trentorise.smartcampus.eb.custom.data.Constants.OBJECT_SERVICE);
		request.setMethod(Method.GET);
		ObjectFilter filter = new ObjectFilter();

		filter.setSkip(0);
		filter.setLimit(100);
		// filter by near me

		filter.setCenter(location);
		filter.setRadius(0.01);
		filter.setFromTime(currentTimeMillis - 1000 * 60 * 60 * 1);
		filter.setToTime(currentTimeMillis + 1000 * 60 * 60 * 3);

		if (filterLocations && !filterEvents) {
			filter.setClassName("eu.trentorise.smartcampus.dt.model.POIObject");
		}
		if (!filterLocations && filterEvents) {
			filter.setClassName("eu.trentorise.smartcampus.dt.model.EventObject");
		}

		String queryStrObject = Utils.convertToJSON(filter);
		String queryString = "filter=" + queryStrObject;
		request.setQuery(queryString);

		MessageResponse response = getInstance().mProtocolCarrier.invokeSync(
				request,
				eu.trentorise.smartcampus.eb.custom.data.Constants.APP_TOKEN,
				getAuthToken());
		String body = response.getBody();
		if (body == null || body.trim().length() == 0) {
			return Collections.emptyList();
		}

		Map<String, List<Map<String, Object>>> map = Utils.convertJSON(body,
				new TypeReference<Map<String, List<Map<String, Object>>>>() {
				});
		ArrayList<NearMeObject> objects = new ArrayList<NearMeObject>();
		if (map != null) {
			for (String key : map.keySet()) {
				// if (types != null && !types.contains(key)) continue;
				List<Map<String, Object>> protos = map.get(key);
				if (protos != null) {
					for (Map<String, Object> proto : protos) {
						objects.add(Utils.convertObjectToData(
								NearMeObject.class, proto));
					}
				}
			}
		}

		return objects;
	}

	public static LocationHelper getLocationHelper() {
		return mLocationHelper;
	}

	public static void setLocationHelper(LocationHelper mLocationHelper) {
		EBHelper.mLocationHelper = mLocationHelper;
	}

	public static ExpCollection findCollection(String collectionId) {
		for (ExpCollection c : getUserPreference().getCollections())
			if (c.getId().equals(collectionId))
				return c;
		return null;
	}
}
