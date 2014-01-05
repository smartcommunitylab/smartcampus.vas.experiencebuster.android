package eu.trentorise.smartcampus.eb.syncadapter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.util.Log;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.storage.DataException;

public class FileSyncService extends Service {
	// constant
	public static final long SYNC_INTERVAL = 120 * 1000; // 2 min

	private static final String TAG = "FileSyncService";

	private Timer mTimer = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// cancel if already existed
		try {
			EBHelper.init(getApplicationContext());
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}

		if (mTimer != null) {
			mTimer.cancel();
		} else {
			// recreate new
			mTimer = new Timer();
		}
		// schedule task
		mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, SYNC_INTERVAL);
	}

	class TimeDisplayTimerTask extends TimerTask {

		@Override
		public void run() {
			try {
				Log.i(TAG, "Trying file synchro");
				EBHelper.getSyncStorage().syncFiles();
				Log.i(TAG, "file synchro tentative done");
			} catch (DataException e) {
				Log.e(TAG, e.getMessage());
			}
		}

	}
}
