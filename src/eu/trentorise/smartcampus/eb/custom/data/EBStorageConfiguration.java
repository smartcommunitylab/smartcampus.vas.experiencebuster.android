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

import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.eb.model.UserPreference;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;

public class EBStorageConfiguration implements StorageConfiguration {
	private static final long serialVersionUID = 906503482979452854L;

	@SuppressWarnings("unchecked")
	private static Class<? extends BasicObject>[] classes = (Class<? extends BasicObject>[]) new Class<?>[] { UserPreference.class, Experience.class};
	private static BeanStorageHelper<UserPreference> prefHelper = new UserPreferenceStorageHelper();
	private static BeanStorageHelper<Experience> expHelper = new ExperienceStorageHelper();

	@Override
	public Class<? extends BasicObject>[] getClasses() {
		return classes;
	}

	@Override
	public String getTableName(Class<? extends BasicObject> cls) throws StorageConfigurationException {
		if (cls.equals(UserPreference.class)) {
			return "preferences";
		}
		if (cls.equals(Experience.class)) {
			return "experience";
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BasicObject> BeanStorageHelper<T> getStorageHelper(Class<T> cls)
			throws StorageConfigurationException {
		if (cls.equals(UserPreference.class)) {
			return (BeanStorageHelper<T>) prefHelper;
		}
		if (cls.equals(Experience.class)) {
			return (BeanStorageHelper<T>) expHelper;
		}
		return null;
	}

}
