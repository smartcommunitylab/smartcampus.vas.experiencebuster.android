package eu.trentorise.smartcampus.eb;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.eb.custom.ExpContentAdapter;
import eu.trentorise.smartcampus.eb.custom.ExperiencesListAdapter;
import eu.trentorise.smartcampus.eb.custom.data.Constants;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.model.Concept;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.remote.RemoteStorage;

public class ViewerActivity extends Activity {

	ExperiencesListAdapter experienceLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("ViewerActivity", "OnCreate");
		setContentView(R.layout.exp_shared);

		Intent data = getIntent();
		String expId = data
				.getStringExtra(getApplicationContext()
						.getString(
								eu.trentorise.smartcampus.android.common.R.string.view_intent_arg_object_id));

		new Loader(getApplicationContext(), this).execute(expId);
	}

	class Loader extends AsyncTask<String, Void, Experience> {
		ProgressDialog progress;
		Context ctx;
		Activity act;

		public Loader(Context ctx, Activity a) {
			this.ctx = ctx;
			act = a;
		}

		@Override
		protected Experience doInBackground(String... params) {
			Log.v("ViewerActivity", "Loader doInBackground");

			String expId = params[0];
			RemoteStorage remoteStorage = new RemoteStorage(
					getApplicationContext(), Constants.APP_TOKEN);
			try {
				remoteStorage.setConfig(EBHelper.getAuthToken(),
						GlobalConfig.getAppUrl(getApplicationContext()),
						Constants.SERVICE);
				if (expId != null) {
					return remoteStorage.getObjectById(expId, Experience.class);
				}
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (DataException e) {
				e.printStackTrace();
			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}

			return null;
		};

		@Override
		protected void onPreExecute() {
			Log.v("ViewerActivity", "Loader onPre");

			// progress = ProgressDialog.show(ctx, null,
			// "Caricamento in corso");
		}

		@Override
		protected void onPostExecute(Experience result) {
			Log.v("ViewerActivity", "Loader onPost");

			ListView list = (ListView) findViewById(R.id.exp_contents_shared);

			if (result != null) {
				if (result.getTitle() != null) {
					((TextView) findViewById(R.id.title_tv_shared))
							.setText(result.getTitle());
				}

				if (result.getDescription() != null) {
					((TextView) findViewById(R.id.description_tv_shared))
							.setText(result.getDescription());
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
						R.layout.exp_contents_row, result.getContents());
				list.setAdapter(content);
			} else {
				setContentView(R.layout.exp_shared_error);
			}
		}
	}
}
