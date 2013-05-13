/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

/**
 * @author mirko perillo
 * 
 */
public class ImageCacheManager {

	private static final int DEFAULT_MAX_SIZE = 50;
	private static final int DEFAULT_CLEAN_ELEMENTS = 10;

	private static int counter = 0;
	private static List<String> history = new ArrayList<String>();
	private static Map<String, Bitmap> cache = new HashMap<String, Bitmap>();

	private ImageCacheManager() {
	}

	public static Bitmap get(String key) {
		return cache.get(key);
	}

	public static boolean insert(String key, Bitmap img) {
		cache.put(key, img);
		history.add(key);
		counter++;
		clean();
		return true;
	}

	public static Bitmap remove(String key) {
		Bitmap removed = cache.remove(key);
		if (removed != null) {
			history.remove(key);
			counter--;
		}
		return removed;
	}

	private static void clean() {
		if (counter > DEFAULT_MAX_SIZE) {
			for (String k : history.subList(0, DEFAULT_CLEAN_ELEMENTS - 1)) {
				remove(k);
			}
		}
	}
}
