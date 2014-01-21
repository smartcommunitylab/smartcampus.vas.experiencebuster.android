package eu.trentorise.smartcampus.eb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.ExpContentAdapter;
import eu.trentorise.smartcampus.eb.custom.ExperiencesListAdapter;
import eu.trentorise.smartcampus.eb.custom.capture.ContentRenderer;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.fragments.experience.ExperiencePager;
import eu.trentorise.smartcampus.eb.model.Concept;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.filestorage.client.model.Token;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class ViewerActivity extends Activity {

	private static final String TAG = "Viewer";

	ExperiencesListAdapter experienceLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exp_shared);

		try {
			EBHelper.init(getApplicationContext());
		} catch (NameNotFoundException e) {
			EBHelper.endAppFailure(this, R.string.app_failure_setup);
		}

		Intent data = getIntent();
		String expId = data
				.getStringExtra(getApplicationContext()
						.getString(
								eu.trentorise.smartcampus.android.common.R.string.view_intent_arg_object_id));

		new SCAsyncTask<String, Void, Experience>(this, new Loader(this))
				.execute(expId);
	}

	class Loader extends AbstractAsyncTaskProcessor<String, Experience> {
		ProgressDialog progress;
		Activity act;
		boolean own = false;

		public Loader(Activity a) {
			super(a);
			this.act = a;
		}

		@Override
		public Experience performAction(String... params)
				throws SecurityException, ConnectionException, Exception {
			String expId = params[0];
			Experience e = EBHelper.findExperienceByEntityId(expId);
			if (e.getContents() != null) {
				for (Content c : e.getContents()) {
					if (c.isStorable()) {
						if (c.getValue() != null) {
							Token resourceToken = EBHelper
									.getSharedResourceURL(c.getValue());
							// if photo,video download thumbnail
							File thumb = new File(c.getAbsolutePathThumbnail());
							if (!thumb.exists() && c.permitThumbnail()) {
								FileOutputStream fout = new FileOutputStream(
										thumb);
								EBHelper.getThumbnail(c.getValue(), fout);
							}
							if (resourceToken != null) {
								c.setLocalValue(resourceToken.getUrl());
							} else {
								Log.w(TAG, String.format(
										"Token for resource %s is null",
										c.getValue()));
							}
						} else {
							Log.w(TAG, String.format(
									"Shared content %s has value null",
									c.getValue()));
						}

					}
				}
			}
			if (e != null)
				own = EBHelper.isOwnExperience(e);
			return e;
		}

		@Override
		public void handleResult(Experience result) {
			ListView list = (ListView) findViewById(R.id.exp_contents_shared);

			if (result != null) {
				if (own) {
					ArrayList<Experience> arrayList = new ArrayList<Experience>(
							1);
					arrayList.add(result);
					Intent i = ExperiencePager.prepareIntent(
							ViewerActivity.this, 0, arrayList);
					startActivityForResult(i, 0);
					return;
				}
				findViewById(R.id.exp_shared_container).setVisibility(
						View.VISIBLE);

				if (result.getTitle() != null) {
					((TextView) findViewById(R.id.title_tv_shared))
							.setText(result.getTitle());
				}

				if (result.getTags() != null)
					((TextView) findViewById(R.id.tags_tv_shared))
							.setText(Concept.toSimpleString(result.getTags()));

				if (result.getCreationTime() > 0) {
					((TextView) findViewById(R.id.date_tv_shared))
							.setText(eu.trentorise.smartcampus.eb.Constants.DATE_FORMATTER
									.format(new Date(result.getCreationTime())));
				}
				if (result.getAddress() != null) {
					((TextView) findViewById(R.id.place_tv_shared))
							.setText(result.getAddress());
				}

				ExpContentAdapter content = new ExpContentAdapter(act,
						R.layout.exp_contents_row, result.getContents(), true);
				list.setAdapter(content);
				list.setOnItemClickListener(new ContentClick(act, result));
			} else {
				Toast.makeText(act, R.string.exp_shared_error,
						Toast.LENGTH_LONG).show();
				finish();
				// setContentView(R.layout.exp_shared_error);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

	class ContentClick implements OnItemClickListener {
		private Activity ctx;
		private Experience exp;

		public ContentClick(Activity ctx, Experience exp) {
			this.ctx = ctx;
			this.exp = exp;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position <= exp.getContents().size() && position >= 0) {
				ContentRenderer.renderExternal(ctx,
						exp.getContents().get(position));
			}
		}

	}
}
