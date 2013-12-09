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
package eu.trentorise.smartcampus.eb.custom.capture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import eu.trentorise.smartcampus.eb.Constants;
import eu.trentorise.smartcampus.eb.Constants.CATCH_TYPES;
import eu.trentorise.smartcampus.eb.QRCodeActivity;
import eu.trentorise.smartcampus.eb.custom.MediaUtils;
import eu.trentorise.smartcampus.eb.custom.capture.content.AudioContent;
import eu.trentorise.smartcampus.eb.custom.capture.content.ImageContent;
import eu.trentorise.smartcampus.eb.custom.capture.content.QRCodeContent;
import eu.trentorise.smartcampus.eb.custom.capture.content.VideoContent;

public class CaptureHelper {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private static final int RC_SELECT_PICTURE = 2000;
	private static final int RC_CAPTURE_IMAGE = 2001;
	private static final int RC_CAPTURE_VIDEO = 2002;
	private static final int RC_CAPTURE_AUDIO = 2003;
	private static final int RC_SELECT_VIDEO = 2004;
	private static final int RC_CAPTURE_QRCODE = 2005;

	private Context mContext = null;
	private int shift = 0;
	private ResultHandler resultHandler = null;

	public interface ResultHandler {
		void startActivityForResult(Intent i, int requestCode);

		void onResult(GrabbedContent value);

		void onCancel();
	}

	public CaptureHelper(Context context, int shift, ResultHandler handler) {
		super();
		this.mContext = context;
		this.shift = shift;
		this.resultHandler = handler;
		assert resultHandler != null;
	}

	public void startCapture(CATCH_TYPES type) {
		if (type != null) {
			switch (type) {
			case QRCODE: {
				Intent intent = new Intent(mContext, QRCodeActivity.class);
				resultHandler.startActivityForResult(intent, RC_CAPTURE_QRCODE
						+ shift);
				break;
			}
			case IMAGE_GALLERY: {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				resultHandler.startActivityForResult(
						Intent.createChooser(intent, "Select Image"),
						RC_SELECT_PICTURE + shift);
				break;
			}
			case VIDEO_GALLERY: {
				Intent intent = new Intent();
				intent.setType("video/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				resultHandler.startActivityForResult(
						Intent.createChooser(intent, "Select Video"),
						RC_SELECT_VIDEO + shift);
				break;
			}
			case AUDIO: {
				Intent intent = new Intent(
						MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				// intent.putExtra(MediaStore.Audio.Media.EXTRA_MAX_BYTES,
				// Constants.FILE_SIZE_LIMIT);
				resultHandler.startActivityForResult(intent, RC_CAPTURE_AUDIO
						+ shift);
				break;
			}
			case IMAGE_CAMERA: {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						Constants.EB_APP_MEDIA_FOLDER);
				
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mediaStorageDir+File.separator + "newImg.jpg")));
				
				resultHandler.startActivityForResult(
						Intent.createChooser(intent, "Capture Image"),
						RC_CAPTURE_IMAGE + shift);
				break;
			}
			case VIDEO_CAMERA: {
				Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getOutputMediaFile("3gp")));
				// intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,
				// Constants.FILE_SIZE_LIMIT);
				// Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
				// intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				resultHandler.startActivityForResult(
						Intent.createChooser(intent, "Capture Video"),
						RC_CAPTURE_VIDEO + shift);
				break;
			}
			default:
				break;
			}
		}
	}

	public void onCaptureResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_SELECT_PICTURE + shift) {
			if (resultCode != 0) {
				Uri imgUri = data.getData();
				try {
					// resultHandler.onResult(new
					// ImageContent(imgUri.toString()));
					resultHandler.onResult(new ImageContent(writeMediaData(
							mContext, imgUri, false)));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading image");
				}

			} else {
				resultHandler.onCancel();
			}
		}
		if (requestCode == RC_SELECT_VIDEO + shift) {
			if (resultCode != 0) {
				Uri imgUri = data.getData();
				try {
					// resultHandler.onResult(new
					// VideoContent(imgUri.toString()));
					resultHandler.onResult(new VideoContent(writeMediaData(
							mContext, imgUri, false)));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading video");
				}

			} else {
				resultHandler.onCancel();
			}
		}
		if (requestCode == RC_CAPTURE_IMAGE + shift) {
			if (resultCode != 0) {
				//Uri imgUri = data.getData();
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						Constants.EB_APP_MEDIA_FOLDER);
				
				File fi = new File(mediaStorageDir+File.separator  + "newImg.jpg");
				
				try {
					// imgUri = writeImageData(mContext, imgUri, "jpg");
					// resultHandler.onResult(new
					// ImageContent(imgUri.toString()));
					Uri imgUri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
							fi.getAbsolutePath(), null, null));
					resultHandler.onResult(new ImageContent(writeMediaData(
							mContext, imgUri, true)));
					fi.delete();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading image");
				}

			} else {
				resultHandler.onCancel();
			}
		}
		if (requestCode == RC_CAPTURE_VIDEO + shift) {
			if (resultCode != 0) {
				
				
				try {
					// resultHandler.onResult(new
					// VideoContent(imgUri.toString()));
					resultHandler.onResult(new VideoContent(data.getData().toString().substring(6)));
//					resultHandler.onResult(new VideoContent(writeMediaData(
//							mContext, imgUri, true)));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading video");
				}

			} else {
				resultHandler.onCancel();
			}
		}
		if (requestCode == RC_CAPTURE_AUDIO + shift) {
			if (resultCode != 0) {
				Uri imgUri = data.getData();
				try {
					resultHandler.onResult(new AudioContent(writeMediaData(
							mContext, imgUri, true)));
					// resultHandler.onResult(new
					// AudioContent(imgUri.toString()));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading audio");
				}

			} else {
				resultHandler.onCancel();
			}
		}
		if (requestCode == RC_CAPTURE_QRCODE + shift) {
			if (resultCode != 0) {
				String s = data.getStringExtra(QRCodeActivity.QR_DATA);
				try {
					resultHandler.onResult(new QRCodeContent(s));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("CaptureHelper", "Error reading qrcode");
				}

			} else {
				resultHandler.onCancel();
			}
		}

	}

	public static String writeMediaData(Context ctx, Uri capturedUri,
			boolean deleteOriginal) {
		String capturedFilePath = MediaUtils.getMediaAbsolutePath(ctx,
				capturedUri);
		File capturedFile = new File(capturedFilePath);
		String fExt = null;
		File newPath = null;
		try {
			fExt = capturedFile.getName().substring(
					capturedFile.getName().lastIndexOf('.') + 1);
		} catch (Exception e1) {
			// do nothing
		}
		try {
			InputStream fileInputStream = ctx.getContentResolver()
					.openInputStream(capturedUri);
			newPath = getOutputMediaFile(fExt);
			cloneFile(fileInputStream, newPath);
		} catch (FileNotFoundException e) {
			return null;
		}

		if (deleteOriginal) {
			// Delete original file from Android's Gallery
			capturedFile.delete();
			MediaUtils.deleteMedia(ctx, capturedUri);
		}
		return newPath.getAbsolutePath();// Uri.fromFile(newPath);
	}

	public static void cloneFile(InputStream currentFileInputStream,
			File newPath) {
		FileOutputStream newFileStream = null;
		try {
			newFileStream = new FileOutputStream(newPath);
			byte[] bytesArray = new byte[1024];
			int length;
			while ((length = currentFileInputStream.read(bytesArray)) > 0) {
				newFileStream.write(bytesArray, 0, length);
			}
			newFileStream.flush();

		} catch (Exception e) {
			Log.e(CaptureHelper.class.getName(),
					"Exception while copying file " + currentFileInputStream
							+ " to " + newPath, e);
		} finally {
			try {
				if (currentFileInputStream != null) {
					currentFileInputStream.close();
				}

				if (newFileStream != null) {
					newFileStream.close();
				}
			} catch (IOException e) {
				// Suppress file stream close
				Log.e(CaptureHelper.class.getName(),
						"Exception occured while closing filestream ", e);
			}
		}
	}

	private static File getOutputMediaFile(String ext) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				Constants.EB_APP_MEDIA_FOLDER);
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("experiencebuster", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "M_" + timeStamp + "." + ext);
		return mediaFile;
	}
}
