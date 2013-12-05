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
import eu.trentorise.smartcampus.eb.model.Concept;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;

public class ExperienceStorageHelper implements BeanStorageHelper<Experience> {

	@Override
	public Experience toBean(Cursor cursor) {
		Experience e = new Experience();
		e.setAddress(cursor.getString(cursor.getColumnIndex("address")));
		e.setCollectionIds(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("collectionIds")), String.class));
		e.setContents(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("contents")), Content.class));
		e.setCreationTime(cursor.getLong(cursor.getColumnIndex("creationTime")));
//		e.setDescription(cursor.getString(cursor.getColumnIndex("description")));
		e.setEntityId(cursor.getString(cursor.getColumnIndex("entityId")));
		e.setId(cursor.getString(cursor.getColumnIndex("id")));
		e.setLocation(new double[]{cursor.getDouble(cursor.getColumnIndex("latitude")),cursor.getDouble(cursor.getColumnIndex("longitude"))});
		e.setTags(Utils.convertJSONToObjects(cursor.getString(cursor.getColumnIndex("tags")), Concept.class));
		e.setTitle(cursor.getString(cursor.getColumnIndex("title")));
		e.setUpdateTime(cursor.getLong(cursor.getColumnIndex("updateTime")));
//		e.setSocialUserId(cursor.getString(cursor.getColumnIndex("socialUserId")));
		return e;
	}

	@Override
	public ContentValues toContent(Experience bean) {
		ContentValues values = new ContentValues();

		if (bean.getCollectionIds() == null) {
			bean.setCollectionIds(new ArrayList<String>());
		}
		if (bean.getContents() == null) {
			bean.setContents(new ArrayList<Content>());
		}
		if (bean.getTags() == null) {
			bean.setTags(new ArrayList<Concept>());
		}
		values.put("address", bean.getAddress());
		values.put("collectionIds", Utils.convertToJSON(bean.getCollectionIds()));
		values.put("contents", Utils.convertToJSON(bean.getContents()));
		values.put("creationTime", bean.getCreationTime());
		values.put("entityId", bean.getEntityId());
		values.put("id", bean.getId());
		values.put("title", bean.getTitle());
		values.put("updateTime", bean.getUpdateTime());
//		values.put("socialUserId", bean.getSocialUserId());

		if (bean.getLocation() != null) {
			values.put("latitude", bean.getLocation()[0]);
			values.put("longitude", bean.getLocation()[1]);
		}
		values.put("tags", Utils.convertToJSON(bean.getTags()));
		return values;
	}

	@Override
	public Map<String, String> getColumnDefinitions() {
		Map<String, String> defs = new HashMap<String, String>();

		defs.put("address", "TEXT");
		defs.put("collectionIds", "TEXT");
		defs.put("contents", "TEXT");
		defs.put("creationTime", "INTEGER");
		defs.put("description", "TEXT");
		defs.put("entityId", "TEXT");
		defs.put("title", "TEXT");
		defs.put("updateTime", "INTEGER");
		defs.put("latitude", "DOUBLE");
		defs.put("longitude", "DOUBLE");
		defs.put("tags", "TEXT");
//		defs.put("socialUserId", "TEXT");
		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

}
