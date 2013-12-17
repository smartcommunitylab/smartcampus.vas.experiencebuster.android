package eu.trentorise.smartcampus.eb.syncadapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FileSyncDatasource {
	// Database fields
	private SQLiteDatabase database;
	private FileSyncDbHelper dbHelper;

	private static final String TAG = "FileSyncDatasource";

	private String[] allColumns = { FileSyncDbHelper.COLUMN_ID,
			FileSyncDbHelper.COLUMN_EXP, FileSyncDbHelper.COLUMN_CONTENT,
			FileSyncDbHelper.COLUMN_PATH, FileSyncDbHelper.COLUMN_STATUS,
			FileSyncDbHelper.COLUMN_FID, FileSyncDbHelper.COLUMN_TENTATIVE };

	public FileSyncDatasource(Context context) {
		dbHelper = new FileSyncDbHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public List<SyncFile> getEntryToProcess() {
		List<SyncFile> result = new ArrayList<SyncFile>();
		Cursor cursor = database.query(FileSyncDbHelper.TABLE_FILESYNC,
				allColumns, "status=? OR status=? OR status=?", new String[] {
						FileSyncDbHelper.ST_TO_UPLOAD,
						FileSyncDbHelper.ST_FAIL_NET,
						FileSyncDbHelper.ST_FAIL_SERVICE }, null, null, null);
		Log.i(TAG, "Element to synchronize: " + cursor.getCount());
		if (cursor.moveToFirst()) {
			do {
				result.add(SyncFile.fromCursor(cursor));
			} while (cursor.moveToNext());
		}

		return result;

	}

	public void removeEntry(Integer idEntry) {
		database.delete(FileSyncDbHelper.TABLE_FILESYNC, "_id=" + idEntry, null);
	}

	public void updateStatus(SyncFile entry, String status)
			throws IllegalArgumentException {
		if (!Arrays.asList(FileSyncDbHelper.ST_FAIL_DB,
				FileSyncDbHelper.ST_FAIL_NET, FileSyncDbHelper.ST_FAIL_SERVICE,
				FileSyncDbHelper.ST_TO_UPLOAD,
				FileSyncDbHelper.ST_FAIL_RESOURCE).contains(status)) {
			throw new IllegalArgumentException(status + " not exist");
		}
		ContentValues cv = new ContentValues();
		cv.put(FileSyncDbHelper.COLUMN_STATUS, status);
		cv.put(FileSyncDbHelper.COLUMN_TENTATIVE, entry.getTentative() + 1);
		if (status.equals(FileSyncDbHelper.ST_FAIL_DB)) {
			cv.put(FileSyncDbHelper.COLUMN_FID, entry.getFid());
		}
		database.update(FileSyncDbHelper.TABLE_FILESYNC, cv,
				"_id=" + entry.getIdEntry(), null);
	}

	public long insertEntry(String idExp, String idContent, String path) {
		// search if entry already present
		String whereCondition = FileSyncDbHelper.COLUMN_EXP + " =?" + " AND "
				+ FileSyncDbHelper.COLUMN_CONTENT + " =?";
		String[] projection = { FileSyncDbHelper.COLUMN_ID };
		Cursor cursor = database.query(FileSyncDbHelper.TABLE_FILESYNC,
				projection, whereCondition, new String[] { idExp, idContent },
				null, null, null);
		long newid = -1;
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put(FileSyncDbHelper.COLUMN_EXP, idExp);
			values.put(FileSyncDbHelper.COLUMN_CONTENT, idContent);
			values.put(FileSyncDbHelper.COLUMN_PATH, path);
			values.put(FileSyncDbHelper.COLUMN_STATUS,
					FileSyncDbHelper.ST_TO_UPLOAD);
			newid = database.insert(FileSyncDbHelper.TABLE_FILESYNC, null,
					values);
			Log.i(FileSyncDatasource.class.getName(),
					"New file added at the table, id: " + newid);
		} else {
			if (cursor.moveToFirst()) {
				newid = cursor.getLong(0);
				Log.i(FileSyncDatasource.class.getName(),
						"File already present in table, id: " + newid);
			}
		}
		return newid;
	}
}
