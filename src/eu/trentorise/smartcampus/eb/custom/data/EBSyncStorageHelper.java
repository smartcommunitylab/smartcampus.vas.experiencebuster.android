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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.SyncData;
import eu.trentorise.smartcampus.storage.sync.SyncStorageHelperWithPaging;

public class EBSyncStorageHelper extends SyncStorageHelperWithPaging {

	public EBSyncStorageHelper(Context context, String dbName, int version, StorageConfiguration config) {
		super(context, dbName, version, config);
	}

	protected void removeOld(int num) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		try {
			Cursor c = db.rawQuery("SELECT COUNT(*) FROM notifications WHERE starred = 0", null);
			c.moveToNext();
			int total = c.getInt(0);
			if (total > num) {
				int toDelete = total - num;
				c = db.rawQuery("SELECT id FROM notifications WHERE starred = 0 ORDER BY timestamp ASC", null);
				c.moveToFirst();
				for (int i = 0; i < toDelete; i++) {
					db.delete("notifications", "id = '" + c.getString(0) + "'", null);
					c.moveToNext();
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public SyncData synchronize(Context ctx, ProtocolCarrier mProtocolCarrier, String authToken, String appToken,
			String host, String service) throws SecurityException, ConnectionException, DataException,
			ProtocolException, StorageConfigurationException {
		SyncData data = super.synchronize(ctx, mProtocolCarrier, authToken, appToken, host, service);
//		int max = Constants.MAX_MESSAGE_NUM;
//		removeOld(max);
		return data;
	}

}
