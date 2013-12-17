package eu.trentorise.smartcampus.eb.syncadapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FileSyncDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "filesync";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_FILESYNC = "filesync";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_EXP = "idExp";
	public static final String COLUMN_CONTENT = "idContent";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_PATH = "path";
	public static final String COLUMN_FID = "fid";
	public static final String COLUMN_TENTATIVE = "tentative";

	public static final String ST_FAIL_RESOURCE = "FRES";
	public static final String ST_FAIL_DB = "FDB";
	public static final String ST_FAIL_NET = "FNET";
	public static final String ST_FAIL_SERVICE = "FSRV";
	public static final String ST_TO_UPLOAD = "TOUP";

	// Database creation sql statement
	private String DATABASE_CREATE;

	public FileSyncDbHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		StringBuffer buffer = new StringBuffer();
		buffer.append("create table ").append(TABLE_FILESYNC).append("(")
				.append(COLUMN_ID).append(" integer primary key autoincrement")
				.append(",").append(COLUMN_EXP).append(" text not null")
				.append(",").append(COLUMN_CONTENT).append(" text not null")
				.append(",").append(COLUMN_PATH).append(" text not null")
				.append(",").append(COLUMN_STATUS).append(" text not null")
				.append(",").append(COLUMN_FID).append(" text").append(",")
				.append(COLUMN_TENTATIVE).append(" integer").append(")");
		DATABASE_CREATE = buffer.toString();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
