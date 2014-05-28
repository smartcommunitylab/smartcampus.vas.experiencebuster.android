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
	public static final String APP_NAME = "local";
	public static final String SERVICE = "/smartcampus.vas.experiencebuster.web";
	public static final String SYNC_SERVICE = "/smartcampus.vas.experiencebuster.web/sync";
	public static final String OBJECT_SERVICE = "/smartcampus.vas.discovertrento.web/objects/simple";
	public static final String FILE_SERVICE = "/core.filestorage";
	public static final String SYNC_DB_NAME = "experiencebusterdb";
	public static final int MAX_MESSAGE_NUM = 1000;
	public static final int DB_VERSION = 1;

	/**
	 * frequency of file sync execution
	 */
	public static final long FILE_SYNC_INTERVAL = 120 * 1000; // 2 min

	/**
	 * max number of uploading file tentatives; after that upload is removed
	 * from filesync table
	 */
	public static final int FILE_SYNC_MAX_TENTATIVES = 5;

	/**
	 * total file uploaded size every execution of filesyncservice
	 */
	public static final long FILE_SYNC_UPLOAD_SIZE = 100000000; // 100 MB
}
