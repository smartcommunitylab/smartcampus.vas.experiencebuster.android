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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ContentType;
import eu.trentorise.smartcampus.filestorage.client.model.Metadata;
import eu.trentorise.smartcampus.filestorage.client.model.Resource;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.storage.AndroidFilestorage;

public class ImageLoadTask extends AsyncTask<Content, Void, Bitmap> {

	private final WeakReference<ImageView> imageViewReference;

	private String tag;
	private AndroidFilestorage filestorage;
	private boolean shared = false;

	private static Map<String, Boolean> loadingHistory = new HashMap<String, Boolean>();

	public ImageLoadTask(ImageView imageView, Integer icMenuReportImage) {
		// Use a WeakReference to ensure the ImageView can be garbage
		// collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		try {
			filestorage = new AndroidFilestorage(
					GlobalConfig.getAppUrl(imageView.getContext()
							.getApplicationContext()) + Constants.FILE_SERVICE,
					Constants.APP_NAME);
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
	}

	public ImageLoadTask(ImageView imageView, Integer icMenuReportImage,
			boolean shared) {
		this(imageView, icMenuReportImage);
		this.shared = shared;
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

			// load from cache if it's present
			img = ImageCacheManager.get(params[0].getId());
			if (img == null) {
				String mediaPath = params[0].getLocalValue().substring(
						params[0].getLocalValue().indexOf("Pictures"));

				File f = new File(Environment.getExternalStorageDirectory(),
						mediaPath);

				// if resource doesn't exist locally, load from remote and save
				// if synchro is active
				if (!f.exists()
						&& !loadingHistory.containsKey(params[0].getId())
						&& EBHelper.isSynchronizationActive()
						&& params[0].isUploaded() && !shared) {
					loadingHistory.put(params[0].getId(), true);
					Metadata meta = filestorage.getResourceMetadataByUser(
							EBHelper.getAuthToken(), params[0].getValue());
					if (EBHelper.checkFileSizeConstraints(meta.getSize())) {
						Resource resource = filestorage.getResourceByUser(
								EBHelper.getAuthToken(), params[0].getValue());
						FileOutputStream fout = new FileOutputStream(f);
						fout.write(resource.getContent());
						fout.close();
						Log.i(ImageLoadTask.class.getName(),
								"Image not present loaded from remote and saved: "
										+ params[0].getLocalValue());
					} else {
						Log.i(ImageLoadTask.class.getName(),
								String.format(
										"Image %s size is bigger than max file configuration : %s > %s",
										params[0].getLocalValue(), meta
												.getSize(),
										EBHelper.getConfiguration(
												EBHelper.CONF_FILE_SIZE,
												String.class)));
					}
				}

				if (!f.exists()
						&& !loadingHistory.containsKey(params[0].getId())
						&& params[0].isUploaded() && shared) {
					Resource resource = filestorage.getSharedResourceByUser(
							EBHelper.getAuthToken(), params[0].getValue());
					FileOutputStream fout = new FileOutputStream(f);
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
					ImageCacheManager.insert(params[0].getId(), img);
					params[0].cache(img);
				}
			}
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(),
					"Exception decoding preview picture");
		}
		return img;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		ImageView imgView = imageViewReference.get();
		if (imgView == null || (tag != null && !tag.equals(imgView.getTag()))) {
			return;
		}
		imgView.setImageBitmap(result);
		loadingHistory.remove(tag);
	}

}
