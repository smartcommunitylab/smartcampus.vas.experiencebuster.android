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
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.SyncStorageHelper;
import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;

public class EBSyncStorage extends SyncStorageWithPaging {

	public EBSyncStorage(Context context, String appToken, String dbName, int dbVersion, StorageConfiguration config) {
		super(context, appToken, dbName, dbVersion, config);
	}

	@Override
	protected SyncStorageHelper createHelper(Context context, String dbName, int dbVersion, StorageConfiguration config) {
		return new EBSyncStorageHelper(context, dbName, dbVersion, config);
	}


	
}
