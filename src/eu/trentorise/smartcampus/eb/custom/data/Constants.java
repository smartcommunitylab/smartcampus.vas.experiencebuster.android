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

public class Constants {

	public static final String APP_TOKEN = "experiencebuster";
	public static final String APP_NAME = "smartcampus";
	public static final String SERVICE = "/smartcampus.vas.experiencebuster.web";
	public static final String SYNC_SERVICE = "/smartcampus.vas.experiencebuster.web/sync";
	public static final String OBJECT_SERVICE = "/smartcampus.vas.discovertrento.web/objects/simple";
	public static final String FILE_SERVICE = "/smartcampus.filestorage";
	public static final long SYNC_INTERVAL = 5 * 60000;
	public static final String SYNC_DB_NAME = "experiencebusterdb";
	public static final int MAX_MESSAGE_NUM = 1000;
}
