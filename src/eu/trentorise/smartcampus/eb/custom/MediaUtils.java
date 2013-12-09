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

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MediaUtils {

	public static Bitmap createBitmap(String absPath, BitmapFactory.Options options) throws IOException {
		Matrix matrix = new Matrix();
		ExifInterface exifReader = new ExifInterface(absPath);
		int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
		if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
		       matrix.postRotate(90);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
		       matrix.postRotate(180);
		} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
		      matrix.postRotate(270);
		}
		Bitmap img = BitmapFactory.decodeFile(absPath, options);
		if (img == null) {
			Log.w(MediaUtils.class.getSimpleName(), "decode bitmap failed");
		} else if (orientation != ExifInterface.ORIENTATION_NORMAL) {
			img = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
		}
		return img;
	}
	
	public static String getMediaAbsolutePath(Context ctx, Uri uri) {
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = ctx.getContentResolver().query(uri,
				filePathColumn, null, null, null);
		if (cursor == null || cursor.getCount() == 0) return null;

		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}

	public static int getMediaOrientation(Context ctx, Uri uri) {
		String[] orientationColumn = { MediaStore.Images.ImageColumns.ORIENTATION };

		Cursor cursor = ctx.getContentResolver().query(uri, orientationColumn, null, null, null);
		if (cursor == null || cursor.getCount() == 0) return -1;

		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(orientationColumn[0]);
		int res = cursor.getInt(columnIndex);
		cursor.close();
		return res;
	}

	public static void deleteMedia(Context ctx, Uri imageUri) {
		ctx.getContentResolver().delete(imageUri, null, null);
	}

	public static Bitmap getBitmap(Context ctx, Uri imageUri, int reqHeight, int reqWidth) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		String imagePath = getMediaAbsolutePath(ctx, imageUri);
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    options.inJustDecodeBounds = false;
	    options.inSampleSize = inSampleSize;
	    bitmap = BitmapFactory.decodeFile(imagePath, options);
	    
		return bitmap;
	}
}
