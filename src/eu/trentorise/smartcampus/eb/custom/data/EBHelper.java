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

import it.smartcampuslab.eb.R;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.AppHelper;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.eb.HomeActivity;
import eu.trentorise.smartcampus.eb.filestorage.FilestorageAccountActivity;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.ExperienceFilter;
import eu.trentorise.smartcampus.eb.model.NearMeObject;
import eu.trentorise.smartcampus.eb.model.Resource;
import eu.trentorise.smartcampus.eb.model.UserPreference;
import eu.trentorise.smartcampus.eb.syncadapter.FileSyncStorage;
import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
import eu.trentorise.smartcampus.filestorage.client.model.Token;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Entity;
import eu.trentorise.smartcampus.storage.AndroidFilestorage;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.remote.RemoteStorage;
import eu.trentorise.smartcampus.territoryservice.TerritoryService;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIData;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class EBHelper {

	public final static int FILESTORAGE_ACCOUNT_REGISTRATION = 20000;

	public static final String CONF_SYNCHRO = "pref_synchro_file";
	public static final String CONF_FILE_SIZE = "pref_max_file_lenght";
	public static final String CONF_USER_ACCOUNT = "EB_USER_ACCOUNT";

	private static final String TERRITORY_URL = "/core.territory";

	private static final String TAG = "EBHelper";

	private static EBHelper instance = null;
	private static RemoteStorage remoteStorage;

	private static SCAccessProvider accessProvider;

	public static Account SCAccount;

	private Context mContext;
	private StorageConfiguration sc = null;
	private FileSyncStorage storage = null;

	private static LocationHelper mLocationHelper;

	private static BasicProfile bp = null;

	private static AndroidFilestorage filestorage;

	private static String FIRST_LAUNCH = "ebfirstlaunch";

	private static String FIRST_SHARE= "eb_first_share";

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

	/**
	 * To call from non UI thread only!
	 * 
	 * @return
	 */
	public static BasicProfile readBasicProfile() {
		if (bp == null) {
			try {
				BasicProfileService bps = new BasicProfileService(
						GlobalConfig.getAppUrl(getInstance().mContext) + "/aac");
				bp = bps.getBasicProfile(getAuthToken());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bp;
	}

	public static synchronized void synchronize(boolean synchronizeFile) {
		if (isSynchronizationActive()) {
			Bundle bundle = new Bundle();
			bundle.putBoolean("synchroFile", synchronizeFile);
			ContentResolver.requestSync(SCAccount,
					"eu.trentorise.smartcampus.eb", bundle);
		}
	}

	/**
	 * Check whether the synchronization is enabled and ask the user if needed.
	 * 
	 * @param activity
	 * @return true if the synchronization is enabled but the user has yet to
	 *         provide approval / credentials, false otherwise
	 * @throws DataException
	 */
	public static boolean ensureSyncConfig(Activity activity)
			throws DataException {
		if (getConfiguration(EBHelper.CONF_SYNCHRO, Boolean.class)
				&& getConfiguration(EBHelper.CONF_USER_ACCOUNT, String.class) == null) {
			askUserAccount(activity, FILESTORAGE_ACCOUNT_REGISTRATION, true);
			return true;
		}
		return false;
	}

	public static void ensureAccount(Activity activity) throws DataException {
		if (getConfiguration(EBHelper.CONF_USER_ACCOUNT, String.class) == null) {
			askUserAccount(activity, FILESTORAGE_ACCOUNT_REGISTRATION, false);
		}
	}

	/**
	 * Handle the result of the account creation activity
	 * 
	 * @param ctx
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public static void handleAccountActivityResult(Context ctx,
			int requestCode, int resultCode, Intent data) {
		if (requestCode == EBHelper.FILESTORAGE_ACCOUNT_REGISTRATION) {
			if (resultCode == Activity.RESULT_OK) {
				String accountId = data
						.getStringExtra(FilestorageAccountActivity.EXTRA_USER_ACCOUNT_ID);
				try {
					EBHelper.saveSyncConfig(true, accountId);
				} catch (DataException e) {
					Toast.makeText(ctx, R.string.error_account,
							Toast.LENGTH_SHORT).show();
					Log.e(HomeActivity.class.getName(),
							"Error saving filestorage account");
				}
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				try {
					EBHelper.saveSyncConfig(false, null);
				} catch (DataException e) {
					Log.e(HomeActivity.class.getName(),
							"Error saving filestorage account");
				}
			}
		}

	}

	/**
	 * Save sync properties in preferences
	 * 
	 * @param active
	 * @param accountId
	 * @throws DataException
	 */
	public static void saveSyncConfig(boolean active, String accountId)
			throws DataException {
		saveConfiguration(EBHelper.CONF_SYNCHRO, active, Boolean.class);
		saveConfiguration(EBHelper.CONF_USER_ACCOUNT, accountId, String.class);
	}

	public static boolean isSynchronizationActive() {
		try {
			return getConfiguration(CONF_SYNCHRO, Boolean.class);
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

	@SuppressWarnings("unchecked")
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
			return resource.getSize() <= maxSize;
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
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_WILDCARD);
		}

		try {
			filestorage = new AndroidFilestorage(
					GlobalConfig.getAppUrl(mContext.getApplicationContext())
							+ Constants.FILE_SERVICE, Constants.APP_NAME);
		} catch (ProtocolException e) {
			Log.e(TAG, "ProtocolException istantiating filestorage");
		}

		this.sc = new EBStorageConfiguration();
		this.storage = new FileSyncStorage(
				mContext,
				eu.trentorise.smartcampus.eb.custom.data.Constants.APP_TOKEN,
				eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_DB_NAME,
				Constants.DB_VERSION, sc);

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
		Collection<UserPreference> userPreferencesCollection = null;
		// if (isSynchronizationActive()) {
		// userPreferencesCollection = readUserPreference();
		// } else {
		userPreferencesCollection = getInstance().storage
				.getObjects(UserPreference.class);
		// }

		if (userPreferencesCollection.isEmpty()) {
			UserPreference userPreference = new UserPreference();
			userPreference.setSocialUserId(-1L);
			userPreference.setCollections(new ArrayList<ExpCollection>());
			getInstance().storage.create(userPreference);
		} else if (userPreferencesCollection.size() > 1) { // fix multiple
															// UserPreference
															// istances
			userPreferencesCollection = getInstance().storage
					.getObjects(UserPreference.class);
			userPreferencesCollection.iterator().next();
		} else {
			userPreferencesCollection.iterator().next();
		}
		// fix
		// if (getInstance().preference.getCollections() == null) {
		// getInstance().preference
		// .setCollections(new ArrayList<ExpCollection>());
		// }
		synchronize(true);
	}

	public static Token getSharedResourceURL(String resourceId) {
		if (filestorage != null) {
			try {
				return filestorage.getSharedResourceTokenByUser(
						EBHelper.getAuthToken(), resourceId);
			} catch (FilestorageException e) {
				Log.e(TAG, "FilestorageException getting resource token");
			} catch (AACException e) {
				Log.e(TAG, "Authentication expcetion getting resource token");
			}
		}
		return null;
	}

	public static void getThumbnail(String resourceId, OutputStream output) {
		if (filestorage != null) {
			try {
				filestorage.getThumbnailByUser(EBHelper.getAuthToken(),
						resourceId, output);
			} catch (FilestorageException e) {
				Log.e(TAG, String.format(
						"FilestorageException getting thumbnail resource %s",
						resourceId));
			} catch (AACException e) {
				Log.e(TAG, String.format(
						"SecurityException getting thumbnail resource %s",
						resourceId));
			}

		}
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
			return getInstance().storage.getObjects(UserPreference.class)
					.iterator().next();
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(), "" + e.getMessage());
			return new UserPreference();
		}
	}

	public static boolean updateUserPreference(Activity a,
			UserPreference userPreference) {
		try {
			getInstance().storage.update(userPreference, false);
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
		if (experienceFilter.getCollectionIds() != null
				&& experienceFilter.getCollectionIds().length > 0) {
			query += (query.length() > 0 ? " AND " : "")
					+ "collectionIds LIKE '%\""
					+ experienceFilter.getCollectionIds()[0] + "\"%'";
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

	public static Experience findExperienceByEntityId(String id) {
		Experience res = findLocalExperienceByEntityId(id);
		if (res == null) {
			try {
				ExperienceFilter filter = new ExperienceFilter();
				filter.setEntityIds(new String[] { id });
				Collection<Experience> coll = getRemote(getInstance().mContext,
						getAuthToken()).searchObjects(filter, Experience.class);
				if (coll != null && coll.size() > 0)
					return coll.iterator().next();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			BasicProfile profile = readBasicProfile();
			if (profile != null)
				res.setSocialUserId(profile.getSocialId());
		}
		return res;
	}

	public static Content findLocalContent(String idExp, String idContent) {
		Experience exp = findLocalExperienceById(idExp);
		if (exp != null) {
			for (Content c : exp.getContents()) {
				if (c.isStorable() && c.getId().equals(idContent)) {
					return c;
				}
			}
		}
		return null;
	}

	public static Experience findLocalExperienceById(String id) {
		String query = "id = '" + id + "'";

		Collection<Experience> collection = null;
		try {
			collection = getInstance().storage.query(Experience.class, query,
					null);
			if (collection != null && collection.size() == 1)
				return collection.iterator().next();
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(), "" + e.getMessage());
		}
		return null;
	}

	public static Experience findLocalExperienceByEntityId(String id) {
		String query = "entityId = '" + id + "'";

		Collection<Experience> collection = null;
		try {
			collection = getInstance().storage.query(Experience.class, query,
					null);
			if (collection != null && collection.size() == 1)
				return collection.iterator().next();
		} catch (Exception e) {
			Log.e(EBHelper.class.getName(), "" + e.getMessage());
		}
		return null;
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
			boolean filterEvents, boolean filterLocations) throws Exception {
		eu.trentorise.smartcampus.territoryservice.model.ObjectFilter filter = new eu.trentorise.smartcampus.territoryservice.model.ObjectFilter();
		filter.setSkip(0);
		filter.setLimit(100);
		// filter by near me

		filter.setCenter(location);
		filter.setRadius(0.01);

		TerritoryService territoryService = new TerritoryService(
				GlobalConfig.getAppUrl(getInstance().mContext) + TERRITORY_URL);

		List<BaseDTObject> objects = new ArrayList<BaseDTObject>();
		if (filterLocations) {
			objects.addAll(territoryService.getPOIs(filter, getAuthToken()));
		}
		Calendar c = Calendar.getInstance();
		if (filterEvents) {
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			filter.setToTime(c.getTimeInMillis());
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			filter.setFromTime(c.getTimeInMillis());
			objects.addAll(territoryService.getEvents(filter, getAuthToken()));
		}

		List<NearMeObject> nearMeObjects = new ArrayList<NearMeObject>();
		for (BaseDTObject object : objects) {
			NearMeObject nmo = new NearMeObject();
			nmo.setTitle(object.getTitle());
			nmo.setDescription(object.getDescription());
			if (object.getFromTime() != null && object.getFromTime() > 0) {
				if (Math.abs(System.currentTimeMillis() - object.getFromTime()) > 24 * 60 * 60 * 1000) {
					nmo.setFromTime(c.getTimeInMillis());
				} else {
					nmo.setFromTime(object.getFromTime());
				}
			}
			if (object instanceof POIObject
					&& ((POIObject) object).getPoi() != null) {
				POIData data = ((POIObject) object).getPoi();
				String s = "";
				if (data.getStreet() != null)
					s += data.getStreet() + " ";
				if (data.getCity() != null)
					s += data.getCity() + " ";
				nmo.setAddress(s);
			}
			nearMeObjects.add(nmo);
		}

		return nearMeObjects;
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

	public static void share(final Experience exp,final Activity ctx) {
		if(PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(FIRST_SHARE , true)){
			showShareDisclaimer(exp, ctx);
			PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(FIRST_SHARE, false).commit();
		}
		else
			callshare(exp, ctx);
	}
	
	private static void showShareDisclaimer(final Experience exp,final Activity ctx){
		WebView wv = new WebView(ctx);
		wv.loadData(ctx.getString(R.string.disclaimer_share), "text/html; charset=UTF-8", "utf-8");
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(android.R.string.dialog_alert_title)
			   .setView(wv)
			   .setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					callshare(exp, ctx);
				}
			   })
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						callshare(exp, ctx);
					}
			   });
		builder.create().show();
	}
	

	public static void callshare(Experience exp, Activity ctx) {
		Entity obj = new Entity();
		obj.setEntityId(exp.getEntityId());
		obj.setTitle(exp.getTitle());
		obj.setEntityType(eu.trentorise.smartcampus.eb.Constants.ENTITY_TYPE_EXPERIENCE);
		Intent intent = new Intent();
		intent.setAction(ctx
				.getString(eu.trentorise.smartcampus.android.common.R.string.share_intent_action));
		intent.putExtra(
				ctx.getString(eu.trentorise.smartcampus.android.common.R.string.share_entity_arg_entity),
				obj);
		AppHelper.startActivityForApp(intent, ctx);
	}

	/**
	 * To call from non UI thread only!
	 * 
	 * @return
	 */
	public static boolean isOwnExperience(Experience e) {
		if (e == null)
			return false;
		BasicProfile profile = readBasicProfile();
		if (profile == null) {
			return false;
		}
		return profile.getSocialId().equals(e.getSocialUserId());
	}

	public static void applyScaleAnimationOnView(final View v) {
		v.postDelayed(new Runnable() {

			@Override
			public void run() {
				ScaleAnimation anim = new ScaleAnimation(0, 1, 1, 1);
				anim.setDuration(320);
				v.startAnimation(anim);
				v.requestFocus();
			}
		}, 10);
	}

	public static void applyAlphaAnimationOnView(final View v) {
		v.post(new Runnable() {

			@Override
			public void run() {
				Animation fadeIn = new AlphaAnimation(0, 1);
				fadeIn.setInterpolator(new AccelerateInterpolator()); // add
																		// this
				fadeIn.setDuration(350);
				v.startAnimation(fadeIn);
			}
		});
	}

	public static void openKeyboard(final Context ctx, final View v) {
		v.postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager keyboard = (InputMethodManager) ctx
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(v, 0);
			}
		}, 10);
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device
	 * density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on
	 *         device density
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method converts device specific pixels to density independent
	 * pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	public static boolean isFirstLaunch(Context ctx) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		return sp.getBoolean(FIRST_LAUNCH, true);
	}

	public static void disableFirstLaunch(Context ctx) {
		PreferenceManager.getDefaultSharedPreferences(ctx).edit()
				.putBoolean(FIRST_LAUNCH, false).commit();
	}

}
