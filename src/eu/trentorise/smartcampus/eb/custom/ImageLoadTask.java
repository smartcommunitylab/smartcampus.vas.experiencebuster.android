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

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ContentType;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.storage.Filestorage;
import eu.trentorise.smartcampus.storage.model.Resource;

public class ImageLoadTask extends AsyncTask<Content, Void, Bitmap> {

	private final WeakReference<ImageView> imageViewReference;
	private Integer resource;

	private String tag;
	private Filestorage filestorage;

	// private ImgHolder holder;

	private static Map<String, Boolean> loadingHistory = new HashMap<String, Boolean>();

	public ImageLoadTask(ImageView imageView, Integer icMenuReportImage) {
		// Use a WeakReference to ensure the ImageView can be garbage
		// collected
		imageViewReference = new WeakReference<ImageView>(imageView);

		try {
			filestorage = new Filestorage(imageView.getContext(),
					Constants.APP_NAME, Constants.APP_TOKEN,
					GlobalConfig.getAppUrl(imageView.getContext()),
					Constants.FILE_SERVICE);
			// this.holder = holder;
		} catch (ProtocolException e) {
			Log.e(ImageLoadTask.class.getName(),
					"Error istantiating filestorage class");
		}

		// this.holder = holder;

	}

	@Override
	protected Bitmap doInBackground(Content... params) {
		Bitmap img = null;

		if (params.length != 1) {
			Log.w(ImageLoadTask.class.getName(),
					"ImageCacheTask expected one parameter");
			return null;
		}

		try {
			tag = params[0].getId();

			File f = new File(params[0].getLocalValue());

			// if resource doesn't exist locally, load from remote and save
			if (!f.exists() && !loadingHistory.containsKey(params[0].getId())) {
				loadingHistory.put(params[0].getId(), true);
				Resource resource = filestorage.getResource(
						EBHelper.getAuthToken(), params[0].getValue());
				FileOutputStream fout = new FileOutputStream(
						params[0].getLocalValue());
				fout.write(resource.getContent());
				fout.close();
				Log.i(ImageLoadTask.class.getName(),
						"Image not present loaded from remote and saved: "
								+ params[0].getLocalValue());
			}
			if (f.exists()) {
				String absPath = f.getAbsolutePath();
				if (absPath == null)
					return null;

				if (params[0].getType() == ContentType.PHOTO) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 4;
					img = MediaUtils.createBitmap(absPath, options);
				} else if (params[0].getType() == ContentType.VIDEO) {
					img = ThumbnailUtils.createVideoThumbnail(absPath,
							MediaStore.Images.Thumbnails.MICRO_KIND);
				}
				params[0].cache(img);
			}
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(),
					"Exception decoding preview picture");
		}
		return img;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null) {
			// if (holder == null) {
			ImageView imgView = imageViewReference.get();
			if (imgView == null
					|| (tag != null && !tag.equals(imgView.getTag()))) {
				return;
			}
			imgView.setImageBitmap(result);
			// } else {
			// holder.preview.setImageBitmap(result);
			// }
			loadingHistory.remove(tag);
		}
	}

}
