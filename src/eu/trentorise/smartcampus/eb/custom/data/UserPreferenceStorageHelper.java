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
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.UserPreference;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;

public class UserPreferenceStorageHelper implements BeanStorageHelper<UserPreference> {

	@Override
	public UserPreference toBean(Cursor cursor) {
		UserPreference pref = new UserPreference();
		pref.setId(cursor.getString(cursor.getColumnIndex("id")));
		pref.setCollections(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("collections")),
				ExpCollection.class));
		pref.setSocialUserId(cursor.getLong(cursor.getColumnIndex("socialUserId")));
		return pref;
	}

	@Override
	public ContentValues toContent(UserPreference bean) {
		ContentValues values = new ContentValues();

		if (bean.getCollections() == null) {
			bean.setCollections(new ArrayList<ExpCollection>());
		}
		values.put("id", bean.getId());
		values.put("collections", Utils.convertToJSON(bean.getCollections()));
		values.put("socialUserId", bean.getSocialUserId());
		return values;
	}

	@Override
	public Map<String, String> getColumnDefinitions() {
		Map<String, String> defs = new HashMap<String, String>();

		defs.put("collections", "TEXT");
		defs.put("socialUserId", "INTEGER");
		return defs;
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

}
