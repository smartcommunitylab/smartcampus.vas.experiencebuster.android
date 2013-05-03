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
package eu.trentorise.smartcampus.eb.custom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Resource;

public class Utils {
	private static final String TAG = "Utils";

	public static String getShortAddressString(Address a) {
		String res = a.getLocality();
		if (res == null || res.length() == 0)
			return a.getAddressLine(0);
		return res;
	}

	public static Address getCurrentPlace(Context ctx) {
		GeoPoint me = requestMyLocation(ctx);
		if (me == null)
			return null;
		Address a = findAddress(me, ctx);
		if (a == null) {
			a = new Address(Locale.getDefault());
			a.setAddressLine(0, "");
			a.setLatitude(me.getLatitudeE6() / 1E6);
			a.setLongitude(me.getLongitudeE6() / 1E6);
		}
		return a;
	}

	public static GeoPoint requestMyLocation(Context ctx) {
		return EBHelper.getLocationHelper().getLocation();
	}

	private static Address findAddress(GeoPoint p, Context contexto) {
		SCGeocoder geoCoder = new SCGeocoder(contexto, Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocationSC(
					p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6, true);
			if (addresses == null || addresses.isEmpty())
				return null;
			return addresses.get(0);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String generateUID() {
		return UUID.randomUUID().toString();
	}

	public static Resource getResource(Context ctx, String uri) {
		byte[] buffer = new byte[512];
		String contentType = null;
		File res = new File(uri);
		FileInputStream fis;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		String ext = MimeTypeMap.getFileExtensionFromUrl(uri);
		String name = getFilename(uri, true);
		Resource resource = null;
		if (ext != null) {
			contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					ext);
		}
		try {
			fis = new FileInputStream(res);
			int readed = -1;
			while ((readed = fis.read(buffer, 0, buffer.length)) != -1) {
				bout.write(buffer, 0, readed);
			}
			resource = new Resource(bout.toByteArray(), contentType, name);
			Log.i(TAG, String.format("Loaded resource %s of type %s", name,
					contentType));
		} catch (IOException e) {
			Log.i(TAG, String.format("Problem loading resource %s of type %s",
					name, contentType));
		}
		return resource;
	}

	public static String getFilename(String uri, boolean extension) {
		int start = uri.lastIndexOf("/") + 1;
		int end = uri.lastIndexOf(".") - 1;
		return uri.substring(start, (extension ? uri.length() : end));
	}

}
