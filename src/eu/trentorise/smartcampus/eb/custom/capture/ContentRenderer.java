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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.view.ViewHelper;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.EmbeddedMediaPlayer;
import eu.trentorise.smartcampus.eb.custom.ImageLoadTask;
import eu.trentorise.smartcampus.eb.custom.Utils;
import eu.trentorise.smartcampus.eb.model.Content;

@SuppressLint("NewApi")
public class ContentRenderer {

	private static LayoutParams wrap() {
		return new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	private static LayoutParams match() {
		return new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
	}

	public static ResourceHandler render(Content content, final ViewGroup view,
			int position, boolean shared) {
		switch (content.getType()) {
		case PHOTO:
			ImageView iv = new ImageView(view.getContext());
			iv.setLayoutParams(wrap());
			view.setLayoutParams(wrap());
			((LinearLayout.LayoutParams) view.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
			Display d = ((Activity) view.getContext()).getWindowManager()
					.getDefaultDisplay();
			int mpwidth = d.getWidth();
			iv.getLayoutParams().width = (int) (0.8 * mpwidth);
			iv.getLayoutParams().height = (int) (iv.getLayoutParams().width / 2);
			iv.setScaleType(ScaleType.CENTER_INSIDE);

			Bitmap bitmap = null;
			if (content.cached() != null) {
				bitmap = (Bitmap) content.cached();
				iv.setImageBitmap(bitmap);
			} else {
				iv.setTag(content.getId());
				// if shared content not download it
				if (!shared) {
					// if device use android 3, use parallel async execution
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						new ImageLoadTask(iv, R.drawable.ic_menu_report_image,
								shared).executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR, content);
					} else {// serial otherwise
						new ImageLoadTask(iv, R.drawable.ic_menu_report_image,
								shared).execute(content);
					}
				}
			}
			view.addView(iv);
			return null;
		case TEXT:
			TextView tv = new TextView(view.getContext());
			view.setLayoutParams(match());
			if (position % 2 == 1) {
				tv.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.black));
			} else {
				tv.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.white));
			}
			tv.setLayoutParams(wrap());
			((LinearLayout.LayoutParams) view.getLayoutParams()).gravity = Gravity.LEFT;
			tv.setAutoLinkMask(Linkify.ALL);
			tv.setText(content.getValue());
			view.addView(tv);
			return null;
		case QRCODE:
			TextView qv = new TextView(view.getContext());
			view.setLayoutParams(match());
			if (position % 2 == 1) {
				qv.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.black));
			} else {
				qv.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.white));
			}
			qv.setLayoutParams(wrap());
			((LinearLayout.LayoutParams) view.getLayoutParams()).gravity = Gravity.LEFT;
			qv.setAutoLinkMask(Linkify.ALL);
			qv.setText(content.getValue());
			view.addView(qv);
			return null;
		case OBJECT:
			TextView ov = new TextView(view.getContext());
			view.setLayoutParams(match());
			if (position % 2 == 1) {
				ov.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.black));
			} else {
				ov.setTextColor(view.getContext().getResources()
						.getColor(android.R.color.white));
			}
			ov.setLayoutParams(wrap());
			((LinearLayout.LayoutParams) view.getLayoutParams()).gravity = Gravity.LEFT;
			ov.setAutoLinkMask(Linkify.ALL);
			ov.setText(Html.fromHtml(content.getLocalValue()));
			view.addView(ov);
			return null;
		case VIDEO:
			ImageView vv = new ImageView(view.getContext());
			vv.setLayoutParams(wrap());
			view.setLayoutParams(wrap());
			((LinearLayout.LayoutParams) view.getLayoutParams()).gravity = 0x01;
			d = ((Activity) view.getContext()).getWindowManager()
					.getDefaultDisplay();
			mpwidth = d.getWidth();
			vv.getLayoutParams().width = (int) (0.8 * mpwidth);
			vv.getLayoutParams().height = (int) (vv.getLayoutParams().width / 2);
			vv.setScaleType(ScaleType.CENTER_INSIDE);

			Bitmap thumbnail = null;
			if (content.cached() != null) {
				thumbnail = (Bitmap) content.cached();
				vv.setImageBitmap(thumbnail);
			} else {
				vv.setTag(content.getId());
				new ImageLoadTask(vv, R.drawable.ic_menu_report_image, shared)
						.execute(content);
			}

			view.addView(vv);
			return null;
		case FILE:
			// TODO files to manage
			return null;
		case AUDIO:
			EmbeddedMediaPlayer mp = null;
			view.setLayoutParams(match());
			mp = new EmbeddedMediaPlayer(view, content.getLocalValue());
			View mv = mp.getView();
			view.addView(mv);
			content.cache(mp);
			return mp;
		default:
			return null;
		}
	}

	public static void renderExternal(Activity ctx, Content content) {
		switch (content.getType()) {
		case PHOTO: {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			String path = content.getLocalValue();// MediaUtils.getMediaAbsolutePath(ctx,
													// Uri.parse(content.getLocalValue()));
			if (path == null)
				return;
			if (Utils.isRemote(path)) {
				intent.setData(Uri.parse(path));
			} else {
				File file = new File(path);
				intent.setDataAndType(Uri.fromFile(file), "image/*");
			}
			ctx.startActivity(intent);
			break;
		}
		case VIDEO: {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			String path = content.getLocalValue();// MediaUtils.getMediaAbsolutePath(ctx,
													// Uri.parse(content.getLocalValue()));
			Uri uri = null;
			if (path == null)
				return;
			if (Utils.isRemote(path)) {
				uri = Uri.parse(path);
			} else {
				File file = new File(path);
				uri=Uri.fromFile(file);
			}
			try{
				intent.setDataAndType(uri,"video/*");
				ctx.startActivity(intent);
			}catch (ActivityNotFoundException e) {
				Log.w(ContentRenderer.class.getName(), e.toString());
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				ctx.startActivity(intent);
			}
			
			break;
		}
		case OBJECT: {
			ViewHelper.viewInApp(ctx, content.getEntityType(),
					content.getEntityId(), null);
			break;
		}
		case FILE:
			// TODO files to manage
		}
	}
}
