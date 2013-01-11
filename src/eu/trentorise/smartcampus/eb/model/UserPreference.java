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
package eu.trentorise.smartcampus.eb.model;

import java.util.ArrayList;
import java.util.List;

import eu.trentorise.smartcampus.storage.BasicObject;

public class UserPreference extends BasicObject {
	private static final long serialVersionUID = -4771192399208749425L;
	private long socialUserId;
	private List<ExpCollection> collections;

	public long getSocialUserId() {
		return socialUserId;
	}

	public void setSocialUserId(long socialUserId) {
		this.socialUserId = socialUserId;
	}

	public List<ExpCollection> getCollections() {
		return collections;
	}

	public void setCollections(List<ExpCollection> collections) {
		this.collections = collections;
	}

	public String collectionNames(List<String> ids) {
		if (ids == null || collections == null || collections.isEmpty())
			return null;
		String res = "";
		for (ExpCollection ec : collections) {
			if (ids.contains(ec.getId())) {
				if (res.length() > 0)
					res += ", ";
				res += ec.getName();
			}
		}
		return res;
	}

	public List<String> collectionsNamesList(List<String> ids) {
		List<String> list = new ArrayList<String>();

		if (ids != null && !ids.isEmpty() && collections != null && !collections.isEmpty()) {
			for (ExpCollection ec : collections) {
				if (ids.contains(ec.getId())) {
					list.add(ec.getName());
				}
			}
		}

		return list;
	}
}
