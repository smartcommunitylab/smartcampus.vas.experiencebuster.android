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
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.eb.model.NearMeObject;
import eu.trentorise.smartcampus.eb.model.ObjectFilter;
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

public class EBHelper {

	private static final boolean testing = true;

	private static EBHelper instance = null;

	private static SCAccessProvider accessProvider = new AMSCAccessProvider();

	public static Account SCAccount;
	// = new Account(
	// eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME_DEFAULT,
	// eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE_DEFAULT);

	// private SyncManager mSyncManager;
	private Context mContext;
	private StorageConfiguration sc = null;
	// private EBSyncStorage storage = null;
	private FileSyncStorage storage = null;
	private ProtocolCarrier mProtocolCarrier = null;

	private static LocationHelper mLocationHelper;

	private UserPreference preference = null;
	private boolean loaded = false;

	public static void init(Context mContext) {
		if (instance == null) {
			instance = new EBHelper(mContext);
		}
	}

	public static FileSyncStorage getSyncStorage() throws DataException {
		return getInstance().storage;
	}

	public static synchronized void synchronize(boolean synchronizeFile) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("synchroFile", synchronizeFile);
		ContentResolver.requestSync(SCAccount, "eu.trentorise.smartcampus.eb",
				bundle);
	}

	public static String getAuthToken() {
		return getAccessProvider().readToken(instance.mContext, null);
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

	protected EBHelper(Context mContext) {
		super();
		this.mContext = mContext;
		// this.mSyncManager = new SyncManager(mContext,
		// SyncStorageService.class);
		this.sc = new EBStorageConfiguration();
		this.storage = new FileSyncStorage(mContext, Constants.APP_TOKEN,
				Constants.SYNC_DB_NAME, 1, sc);
		// this.storage = new EBSyncStorage(mContext, Constants.APP_TOKEN,
		// Constants.SYNC_DB_NAME, 1, sc);
		this.mProtocolCarrier = new ProtocolCarrier(mContext,
				Constants.APP_TOKEN);

		SCAccount = new Account(
				eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
				eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));

		// LocationManager locationManager = (LocationManager)
		// mContext.getSystemService(Context.LOCATION_SERVICE);
		// locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
		// 0, 0, new EBLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		// 0, 0, new EBLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0, new EBLocationListener());
		setLocationHelper(new LocationHelper(mContext));
	}

	public static void start() throws RemoteException, DataException,
			StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException {
		if (testing) {
			// UserPreference
			Collection<UserPreference> userPreferencesCollection = getInstance().storage
					.getObjects(UserPreference.class);
			if (userPreferencesCollection.isEmpty()) {
				UserPreference userPreference = new UserPreference();
				userPreference.setSocialUserId(1L);
				getInstance().preference = getInstance().storage
						.create(userPreference);
			} else {
				getInstance().preference = userPreferencesCollection.iterator()
						.next();
			}

			// // some Experiences
			// Collection<Experience> experiencesCollection =
			// getInstance().getstorage.getObjects(Experience.class);
			// if (experiencesCollection.isEmpty()) {
			// UserPreference userPreference = new UserPreference();
			// userPreference.setSocialUserId(1L);
			// List<ExpCollection> collections = Arrays.asList(new
			// ExpCollection[] {
			// new ExpCollection("id1", "name 1"), new ExpCollection("id2",
			// "name 2"),
			// new ExpCollection("id3", "name 3") });
			// userPreference.setCollections(collections);
			// getInstance().preference =
			// getInstance().storage.create(userPreference);
			// } else {
			// getInstance().preference =
			// experiencesCollection.iterator().next();
			// }

			getInstance().loaded = true;
		} else {
			getInstance().loadData();
			// if (getPreferences().isSynchronizeAutomatically()) {
			synchronize(true);
			// }
		}
	}

	private void loadData() throws DataException,
			StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException, RemoteException {
		if (loaded) {
			return;
		}

		loaded = true;
	}

	// public static void synchronize() throws RemoteException, DataException,
	// StorageConfigurationException, SecurityException,
	// ConnectionException, ProtocolException {
	// getInstance().storage.synchronize(getAuthToken(),
	// GlobalConfig.getAppUrl(getInstance().mContext),
	// Constants.SYNC_SERVICE);
	// }

	public static void synchronizeInBG() throws RemoteException, DataException,
			StorageConfigurationException, SecurityException,
			ConnectionException, ProtocolException {
		synchronize(true);
		// getInstance().mSyncManager.synchronize(getAuthToken(),
		// Constants.APP_TOKEN);
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
			DataException {
		return SuggestionHelper.getSuggestions(suggest, getInstance().mContext,
				GlobalConfig.getAppUrl(getInstance().mContext), getAuthToken(),
				Constants.APP_TOKEN);
	}

	public static List<Experience> getExperiences(int position, int size) {
		return findExperiences(new ExperienceFilter(), position, size);
	}

	// public static List<Experience> getExperiencesByCollection(String
	// collectionId, int position, int size) {
	// ExperienceFilter f = new ExperienceFilter();
	// f.setCollectionId(collectionId);
	// return findExperiences(f, position, size);
	// }

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
				Constants.OBJECT_SERVICE);
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
				request, Constants.APP_TOKEN, getAuthToken());
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
