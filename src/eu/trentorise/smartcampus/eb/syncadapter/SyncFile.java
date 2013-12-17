package eu.trentorise.smartcampus.eb.syncadapter;

import android.database.Cursor;

public class SyncFile {
	private Integer idEntry;
	private String idExp;
	private String idContent;
	private String path;
	private String status;
	private String fid;
	private Integer tentative;

	public String getIdExp() {
		return idExp;
	}

	public void setIdExp(String idExp) {
		this.idExp = idExp;
	}

	public String getIdContent() {
		return idContent;
	}

	public void setIdContent(String idContent) {
		this.idContent = idContent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public Integer getTentative() {
		return tentative;
	}

	public void setTentative(Integer tentative) {
		this.tentative = tentative;
	}

	public static SyncFile fromCursor(Cursor cursor) {
		try {
			SyncFile syncFile = new SyncFile();
			syncFile.setIdEntry(cursor.getInt(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_ID)));
			syncFile.setIdExp(cursor.getString(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_EXP)));
			syncFile.setIdContent(cursor.getString(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_CONTENT)));
			syncFile.setFid(cursor.getString(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_FID)));
			syncFile.setPath(cursor.getString(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_PATH)));
			syncFile.setStatus(cursor.getString(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_STATUS)));
			syncFile.setTentative(cursor.getInt(cursor
					.getColumnIndex(FileSyncDbHelper.COLUMN_TENTATIVE)));
			return syncFile;
		} catch (Exception e) {
			return null;
		}
	}

	public Integer getIdEntry() {
		return idEntry;
	}

	public void setIdEntry(Integer idEntry) {
		this.idEntry = idEntry;
	}

}
