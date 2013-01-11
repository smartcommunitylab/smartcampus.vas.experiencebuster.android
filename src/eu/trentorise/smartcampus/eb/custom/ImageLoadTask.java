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
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ContentType;

public class ImageLoadTask extends AsyncTask<Content, Void, Bitmap> {

	private final WeakReference<ImageView> imageViewReference;
	private Integer resource;

	private String tag; 
	
	public ImageLoadTask(ImageView imageView, Integer icMenuReportImage) {
		// Use a WeakReference to ensure the ImageView can be garbage
		// collected
		imageViewReference = new WeakReference<ImageView>(imageView);
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
			String absPath = f.getAbsolutePath();
//			String absPath = BitmapUtils.getBitmapAbsolutePath(imageViewReference.get().getContext(), Uri.parse(params[0].getLocalValue()));
			if (absPath == null) return null;
			
			if (params[0].getType() == ContentType.PHOTO) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
				img = MediaUtils.createBitmap(absPath, options);
			} else if (params[0].getType() == ContentType.VIDEO) {
				img = ThumbnailUtils.createVideoThumbnail(absPath, MediaStore.Images.Thumbnails.MICRO_KIND);
			}
			params[0].cache(img);

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(this.getClass().getSimpleName(),
					"Exception decoding preview picture");
		}
		return img;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		ImageView imgView = imageViewReference.get();
		if (imgView == null || (tag != null && !tag.equals(imgView.getTag()))) return;
		
		if (result != null) {
			imgView.setImageBitmap(result);
		} else if (resource != null) {
			imgView.setImageResource(resource);
		}
	}

}
