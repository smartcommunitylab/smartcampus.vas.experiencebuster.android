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

package eu.trentorise.smartcampus.eb.syncadapter;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

/**
 * SyncAdapter implementation for sync sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class EBSyncAdapter extends AbstractThreadedSyncAdapter {
	private static final String TAG = "EBSyncAdapter";

	private static final int ACCOUNT_NOTIFICATION_ID = 1;
	private final Context mContext;

	public EBSyncAdapter(Context context, boolean autoInitialize)
			throws NameNotFoundException {
		super(context, autoInitialize);
		mContext = context;
		EBHelper.init(mContext);
		ContentResolver.setSyncAutomatically(EBHelper.SCAccount,
				"eu.trentorise.smartcampus.eb", true);
		// ContentResolver.addPeriodicSync(EBHelper.SCAccount,
		// "eu.trentorise.smartcampus.eb", new Bundle(),
		// Constants.SYNC_INTERVAL * 60);

	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		try {
			Log.i(TAG, "Trying synchronization");
			FileSyncStorage storage = EBHelper.getSyncStorage();
			boolean synchroFile = extras.getBoolean("synchroFile");
			Log.i(TAG, "File synchronization: " + synchroFile);
			if (synchroFile) {
				storage.synchroFile(
						EBHelper.getAuthToken(),
						GlobalConfig.getAppUrl(mContext),
						eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_SERVICE);
			} else {
				storage.synchronize(
						EBHelper.getAuthToken(),
						GlobalConfig.getAppUrl(mContext),
						eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_SERVICE);
			}
			Log.i(TAG, "Synchronization started");
		} catch (SecurityException e) {
			handleSecurityProblem();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "on PerformSynch Exception: " + e.getMessage());
		}
	}

	public void handleSecurityProblem() {
		Intent i = new Intent("eu.trentorise.smartcampus.START");
		i.setPackage(mContext.getPackageName());

		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = eu.trentorise.smartcampus.eb.R.drawable.stat_notify_error;
		CharSequence tickerText = mContext
				.getString(eu.trentorise.smartcampus.eb.R.string.token_expired);
		long when = System.currentTimeMillis();
		CharSequence contentText = mContext
				.getString(eu.trentorise.smartcampus.eb.R.string.token_required);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, i,
				0);

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, tickerText, contentText,
				contentIntent);

		mNotificationManager.notify(ACCOUNT_NOTIFICATION_ID, notification);
	}

}
