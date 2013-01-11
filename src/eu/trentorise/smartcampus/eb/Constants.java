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
package eu.trentorise.smartcampus.eb;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

	public enum CATCH_TYPES {
		TEXT, QRCODE, IMAGE_CAMERA, VIDEO_CAMERA, IMAGE_GALLERY, VIDEO_GALLERY, AUDIO, FILE, NEARME
	}

	public static final int FILE_SIZE_LIMIT = 1024 * 1024; // 1Mb

	public static final String ENTITY_TYPE_EXPERIENCE = "experience";
	
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
	public static final SimpleDateFormat DATE_FORMATTER_HEADER = new SimpleDateFormat("MMMM dd", Locale.US);

	public static final String EB_APP_MEDIA_FOLDER = "experiencebuster";

}
