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
package eu.trentorise.smartcampus.eb.fragments.experience;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.sharing.ShareEntityObject;
import eu.trentorise.smartcampus.android.common.sharing.SharingHelper;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.CatchActivity;
import eu.trentorise.smartcampus.eb.Constants;
import eu.trentorise.smartcampus.eb.Constants.CATCH_TYPES;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.eb.custom.ExpContentAdapter;
import eu.trentorise.smartcampus.eb.custom.Utils;
import eu.trentorise.smartcampus.eb.custom.capture.CaptureHelper;
import eu.trentorise.smartcampus.eb.custom.capture.CaptureHelper.ResultHandler;
import eu.trentorise.smartcampus.eb.custom.capture.ContentRenderer;
import eu.trentorise.smartcampus.eb.custom.capture.GrabbedContent;
import eu.trentorise.smartcampus.eb.custom.data.EBHelper;
import eu.trentorise.smartcampus.eb.filestorage.FilestorageAccountActivity;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.eb.model.Concept;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ContentType;
import eu.trentorise.smartcampus.eb.model.ExpCollection;
import eu.trentorise.smartcampus.eb.model.Experience;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.sync.SyncData;

@SuppressLint("NewApi")
public class EditExpMuseFragment extends SherlockFragment implements OnTagsSelectedListener, OnEditListener, ResultHandler,
		NoteHandler, eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback,
		eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback,
		CollectionSavedHandler, PositionHandler {

	public static final String ARG_EXP = "exp";
	public static final String ARG_VALUE = "value";
	public static final String ARG_BACK = "back";

	private TextEditSwitch mTitleSwitch, mDescrSwitch;

	private GrabbedContent grabbedContent;
	private Experience exp = null;

	private boolean editMode = false;
	private boolean backFromCapture = false;

	private CaptureHelper mHelper = null;

	private ExpContentAdapter adapter = null;

	private View returnView;

	public static final int ACCOUNT_CREATION = 10000;

	public static Bundle prepareArgs(Experience e, GrabbedContent content, Boolean backFromCapture) {
		Bundle b = new Bundle();
		if (e != null) {
			b.putSerializable(ARG_EXP, e);
		}
		if (content != null) {
			b.putSerializable(ARG_VALUE, content);
		}
		if (backFromCapture != null) {
			b.putBoolean(ARG_BACK, backFromCapture);
		}
		return b;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new CaptureHelper(getSherlockActivity(), 10, this);

		boolean found = false;

		if (savedInstanceState == null && getArguments() != null && getArguments().containsKey(ARG_VALUE)) {
			grabbedContent = (GrabbedContent) getArguments().getSerializable(ARG_VALUE);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_EXP)) {
			exp = (Experience) savedInstanceState.getSerializable(ARG_EXP);
		} else if (getArguments() != null && getArguments().containsKey(ARG_EXP)) {
			exp = (Experience) getArguments().getSerializable(ARG_EXP);
		} else if (grabbedContent != null && (savedInstanceState == null || !savedInstanceState.containsKey(ARG_EXP))) {
			exp = findExperienceByContentEntity(grabbedContent.toContent().getEntityType(), grabbedContent.toContent()
					.getEntityId());
			if (exp != null) {
				found = true;
			}
		}

		if (exp == null) {
			exp = new Experience();
		}

		if (exp.getContents() == null) {
			exp.setContents(new ArrayList<Content>());
		}

		if (!found) {
			appendContent(grabbedContent);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey("editMode")) {
			editMode = savedInstanceState.getBoolean("editMode");
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_BACK)) {
			backFromCapture = savedInstanceState.getBoolean(ARG_BACK);
		} else if (getArguments() != null && getArguments().containsKey(ARG_BACK)) {
			backFromCapture = getArguments().getBoolean(ARG_BACK);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		returnView = inflater.inflate(R.layout.exp_form, container, false);
		ListView list = (ListView) returnView.findViewById(R.id.exp_contents);
		registerForContextMenu(list);
		if (list.getFooterViewsCount() == 0) {
			View footer = getSherlockActivity().getLayoutInflater().inflate(R.layout.exp_form_footer, null);
			list.addFooterView(footer, null, false);
		}
		if (list.getHeaderViewsCount() == 0) {
			View header = getSherlockActivity().getLayoutInflater().inflate(R.layout.exp_form_header, null);
			list.addHeaderView(header, null, false);
			mTitleSwitch = new TextEditSwitch(returnView, R.id.title_switcher, R.id.title_tv, R.id.title, this);
			mTitleSwitch.setValue(exp.getTitle());
			mDescrSwitch = new TextEditSwitch(returnView, R.id.descr_switcher, R.id.description_tv, R.id.description, this);
			mDescrSwitch.setValue(exp.getDescription());
		}
		adapter = new ExpContentAdapter(getSherlockActivity(), R.layout.exp_contents_row, exp.getContents());
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position <= exp.getContents().size() && position > 0) {
					ContentRenderer.renderExternal(getActivity(), exp.getContents().get(position - 1));
				}
			}

		});

		updateCollectionTV();
		if (exp.getId() == null) {
			new LoadAddressTask().execute();
		} else {
			updateFooterTV(exp.getAddress(), exp.getCreationTime());
		}
		if (exp.getTags() != null) {
			((TextView) returnView.findViewById(R.id.tags_tv)).setText(Concept.toSimpleString(exp.getTags()));
		}

		((TextView) returnView.findViewById(R.id.tags_tv)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TaggingDialogFragment taggingDialog = new TaggingDialogFragment();
				taggingDialog.setArguments(TaggingDialogFragment.prepare(Concept.convertToSS(exp.getTags())));
				taggingDialog.show(getActivity().getSupportFragmentManager(), "tags");
			}
		});

		returnView.findViewById(R.id.place_box).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment textFragment = new EditPositionFragment();
				textFragment.setArguments(EditPositionFragment.prepare(exp.getAddress() == null ? "" : exp.getAddress()));
				textFragment.show(getActivity().getSupportFragmentManager(), "exp_position");
			}
		});

		return returnView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(ARG_EXP, exp);
		// outState.putSerializable(ARG_SRC, src);
		outState.putBoolean("editMode", editMode);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onResume() {
		super.onResume();
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.title_expform);

		// auto save
		if (exp.getTitle() == null || exp.getTitle().length() == 0) {
			String generatedTitle = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(System
					.currentTimeMillis());
			mTitleSwitch.setValue(generatedTitle);
			exp.setTitle(generatedTitle);
			exp.setDescription("");
			if (validate(exp)) {
				try {
					if (EBHelper.getConfiguration(EBHelper.CONF_SYNCHRO, Boolean.class)
							&& EBHelper.getConfiguration(EBHelper.CONF_USER_ACCOUNT, String.class) == null) {
						EBHelper.askUserAccount(this, ACCOUNT_CREATION, false);
					} else {
						new SaveTask().execute();
					}
				} catch (DataException e) {
					Log.e(EditExpMuseFragment.class.getName(), "Error creating filestorage account");
				}
			}
		}

		// open grab dialog fragment automatically
		if (!backFromCapture) {
			// FragmentManager fm =
			// getSherlockActivity().getSupportFragmentManager();
			// GrabDialogFragment gd = new GrabDialogFragment();
			// Bundle args = new Bundle();
			// args.putSerializable(GrabDialogFragment.ARG_EXP, exp);
			// gd.setArguments(args);
			// gd.show(fm, "Grab");
			openGrabDialog();
		}

		if (exp != null && adapter != null && exp.getContents() != null && exp.getContents().size() != adapter.getCount()) {
			ListView list = (ListView) returnView.findViewById(R.id.exp_contents);
			adapter = new ExpContentAdapter(getSherlockActivity(), R.layout.exp_contents_row, exp.getContents());
			list.setAdapter(adapter);
		}		
	}

	@Override
	public void onPause() {
		super.onPause();
		exp.setTitle(mTitleSwitch.getValue());
		exp.setDescription(mDescrSwitch.getValue());
	}

	@Override
	public void onDestroy() {
		if (adapter != null) {
			adapter.release();
		}

		if (exp.getContents().size() == 1) {
			EBHelper.deleteExperience(getSherlockActivity(), exp.getId(), false);
		}

		super.onDestroy();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (!this.equals(((DialogCallbackContainer) getActivity()).getNoteHandler())) {
			return;
		}

		menu.clear();
		if (exp == null || exp.getId() == null) {
			// new element menu
			getSherlockActivity().getSupportMenuInflater().inflate(R.menu.exp_menu_new, menu);
		} else if (!editMode) {
			// view mode
			getSherlockActivity().getSupportMenuInflater().inflate(R.menu.exp_menu, menu);
			MenuItem item = menu.findItem(R.id.expmenu_share);
			if (item != null)
				item.setEnabled(exp.getEntityId() > 0).setVisible(exp.getEntityId() > 0);
		} else {
			// edit mode
			getSherlockActivity().getSupportMenuInflater().inflate(R.menu.exp_menu_edit, menu);
			MenuItem item = menu.findItem(R.id.expmenu_share);
			if (item != null)
				item.setEnabled(exp.getEntityId() > 0).setVisible(exp.getEntityId() > 0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
		} else if (item.getItemId() == R.id.expmenu_done || item.getItemId() == R.id.expmenu_save) {
			exp.setTitle(mTitleSwitch.getValue());
			exp.setDescription(mDescrSwitch.getValue());
			if (validate(exp)) {
				try {
					if (EBHelper.getConfiguration(EBHelper.CONF_SYNCHRO, Boolean.class)
							&& EBHelper.getConfiguration(EBHelper.CONF_USER_ACCOUNT, String.class) == null) {
						EBHelper.askUserAccount(this, ACCOUNT_CREATION, false);
					} else {
						new SaveTask().execute();
					}
				} catch (DataException e) {
					Log.e(EditExpMuseFragment.class.getName(), "Error creating filestorage account");
				}
			}
		} else if (item.getItemId() == R.id.expmenu_attach_audio) {
			onCaptureOption(new String[] { CATCH_TYPES.AUDIO.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_camera_photo) {
			onCaptureOption(new String[] { CATCH_TYPES.IMAGE_CAMERA.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_camera_video) {
			onCaptureOption(new String[] { CATCH_TYPES.VIDEO_CAMERA.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_gallery_photo) {
			onCaptureOption(new String[] { CATCH_TYPES.IMAGE_GALLERY.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_gallery_video) {
			onCaptureOption(new String[] { CATCH_TYPES.VIDEO_GALLERY.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_qrcode) {
			onCaptureOption(new String[] { CATCH_TYPES.QRCODE.toString() }, null);
		} else if (item.getItemId() == R.id.expmenu_attach_text) {
			DialogFragment textFragment = new EditNoteFragment();
			textFragment.setArguments(EditNoteFragment.prepare("", exp.getContents().size()));
			textFragment.show(getActivity().getSupportFragmentManager(), "exp_content_note");
		} else if (item.getItemId() == R.id.expmenu_remove) {
			DialogFragment newFragment = new DeleteExperienceFragment();
			newFragment.setArguments(DeleteExperienceFragment.prepare(exp.getId()));
			newFragment.show(getActivity().getSupportFragmentManager(), "exp_delete");
		} else if (item.getItemId() == R.id.expmenu_assign_collection) {
			DialogFragment assignFragment = new AssignCollectionFragment();
			assignFragment.setArguments(AssignCollectionFragment.prepare(exp.getId(), exp.getCollectionIds()));
			assignFragment.show(getActivity().getSupportFragmentManager(), "exp_assign_colls");
		} else if (item.getItemId() == R.id.expmenu_share) {
			ShareEntityObject obj = new ShareEntityObject(exp.getEntityId(), exp.getTitle(), Constants.ENTITY_TYPE_EXPERIENCE);
			SharingHelper.share(getActivity(), obj);
		} else if (item.getItemId() == R.id.expmenu_map || item.getItemId() == R.id.expmenu_export) {
			Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
		} else {
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACCOUNT_CREATION) {
			try {
				if (resultCode == Activity.RESULT_OK) {
					String accountId = data.getStringExtra(FilestorageAccountActivity.EXTRA_USER_ACCOUNT_ID);
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, true, Boolean.class);
					EBHelper.saveConfiguration(EBHelper.CONF_USER_ACCOUNT, accountId, String.class);
				} else {
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, false, Boolean.class);
				}
			} catch (DataException e) {
				Log.e(EditExpMuseFragment.class.getName(), "Error saving configuration: " + EBHelper.CONF_USER_ACCOUNT);
			}
			new SaveTask().execute();
		}

		mHelper.onCaptureResult(requestCode, resultCode, data);
	}

	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		List<Concept> list = Concept.convertSS(suggestions);
		if (list != null)
			exp.setTags(list);
		((TextView) returnView.findViewById(R.id.tags_tv)).setText(Concept.toSimpleString(list));
		switchToEdit();
	}

	@Override
	public void onEdit() {
		editMode = true;
		getSherlockActivity().invalidateOptionsMenu();
	}

	@Override
	public void onResult(GrabbedContent value) {
		backFromCapture = true;
		appendContent(value);
		switchToEdit();
	}

	@Override
	public void onCancel() {
	}

	protected void startCapture(String string) {
		mHelper.startCapture(CATCH_TYPES.valueOf(string));
	}

	private Experience appendContent(GrabbedContent value) {
		Log.e("eemf", "appendContent Contents (before): " + exp.getContents().size());
		if (value.contentType() == ContentType.TEXT) {
			return exp;
		}

		Content c = value.toContent();
		c.setId(Utils.generateUID());
		c.setType(value.contentType());
		c.setTimestamp(System.currentTimeMillis());

		if (exp.getContents() == null)
			exp.setContents(new ArrayList<Content>());
		exp.getContents().add(0, c);
		exp.resetPreview();

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}

		return exp;
	}

	private void updateFooterTV(String address, Long creationTime) {
		if (creationTime != null) {
			((TextView) returnView.findViewById(R.id.date_tv)).setText(Constants.DATE_FORMATTER.format(new Date(creationTime)));
		}
		if (address != null) {
			((TextView) returnView.findViewById(R.id.place_tv)).setText(address);
		}
	}

	private void updateCollectionTV() {
		TextView tv = (TextView) returnView.findViewById(R.id.collections_tv);
		if (exp.getCollectionIds() == null || exp.getCollectionIds().isEmpty()) {
			tv.setVisibility(View.GONE);
		} else {
			String txt = EBHelper.getUserPreference().collectionNames(exp.getCollectionIds());
			if (txt == null || txt.length() == 0) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setVisibility(View.VISIBLE);
				tv.setText(txt);
			}
		}
	}

	private void switchToView() {
		editMode = false;
		mTitleSwitch.viewMode();
		mDescrSwitch.viewMode();
		getSherlockActivity().invalidateOptionsMenu();
	}

	private void switchToEdit() {
		if (editMode)
			return;
		editMode = true;
		getSherlockActivity().invalidateOptionsMenu();
	}

	private boolean validate(Experience e) {
		if (e.getTitle() == null || e.getTitle().trim().length() == 0) {
			Toast.makeText(getActivity(), R.string.alert_title_required, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private void onCaptureOption(final String[] elems, String[] labelElems) {
		if (elems.length > 1) {
			assert labelElems.length == elems.length;
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setItems(labelElems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startCapture(elems[which]);
					dialog.dismiss();
				}
			});
			AlertDialog dlg = builder.create();
			dlg.setOwnerActivity(getActivity());
			dlg.show();
		} else {
			startCapture(elems[0]);
		}
	}

	private Experience findExperienceByContentEntity(String entityType, Long entityId) {
		List<Experience> list = EBHelper.getExperiences(0, -1);
		for (Experience exp : list) {
			for (Content content : exp.getContents()) {
				if (entityType != null && !entityType.equalsIgnoreCase(content.getEntityType())) {
					continue;
				}

				if (entityId != null && entityId == content.getEntityId()) {
					return exp;
				}
			}
		}

		return null;
	}

	private class LoadAddressTask extends AsyncTask<Void, Void, Address> {
		@Override
		protected Address doInBackground(Void... params) {
			if (exp.getAddress() != null && exp.getLocation() != null) {
				Address a = new Address(Locale.getDefault());
				a.setAddressLine(0, exp.getAddress());
				a.setLatitude(exp.getLocation()[0]);
				a.setLongitude(exp.getLocation()[1]);
				return a;
			}
			return Utils.getCurrentPlace(getSherlockActivity());
		}

		@Override
		protected void onPostExecute(Address result) {
			if (result != null) {
				String addrString = Utils.getShortAddressString(result);
				exp.setAddress(addrString);
				exp.setLocation(new double[] { result.getLatitude(), result.getLongitude() });
				updateFooterTV(addrString, System.currentTimeMillis());
			}
		}
	}

	private class SaveTask extends SCAsyncTask<Void, Void, Experience> {

		public SaveTask() {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<Void, Experience>(getSherlockActivity()) {
				@Override
				public Experience performAction(Void... params) throws SecurityException, ConnectionException, Exception {
					exp.setUpdateTime(System.currentTimeMillis());
					String expId = exp.getId();
					if (expId == null) {
						Address a = Utils.getCurrentPlace(getSherlockActivity());
						if (a == null && exp.getAddress() == null)
							return null;
						if (exp.getAddress() == null) {
							exp.setAddress(Utils.getShortAddressString(a));
						}
						if (a != null) {
							exp.setLocation(new double[] { a.getLatitude(), a.getLongitude() });
						}
						exp.setCreationTime(exp.getUpdateTime());
					}

					if (expId == null) {
						exp = EBHelper.saveExperience(getActivity(), exp, false);
						// find experience updated
						if (EBHelper.isSynchronizationActive()) {
							SyncData data = EBHelper.getSyncStorage().synchroFile(EBHelper.getAuthToken(),
									GlobalConfig.getAppUrl(getActivity().getApplicationContext()),
									eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_SERVICE);
							if (data.getUpdated().get(Experience.class.getCanonicalName()) != null) {
								for (Object o : data.getUpdated().get(Experience.class.getCanonicalName())) {
									Experience updatedExp = eu.trentorise.smartcampus.android.common.Utils.convertObjectToData(
											Experience.class, o);
									if (updatedExp.getId().equals(exp.getId())) {
										return updatedExp;
									}
								}
							}
						}
					} else {
						exp = EBHelper.saveExperience(getActivity(), exp, true);
					}
					return exp;
				}

				@Override
				public void handleResult(Experience result) {
					if (result != null) {
						exp = result;
						if (exp.getId() != null)
							switchToView();
						updateFooterTV(exp.getAddress(), exp.getCreationTime());
						// exp.copyTo(src);
					} else {
						Toast.makeText(getSherlockActivity(), R.string.msg_location_undefined, Toast.LENGTH_SHORT).show();
					}
				}

			});
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (!this.equals(((DialogCallbackContainer) getActivity()).getNoteHandler())) {
			return false;
		}

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (info.position <= exp.getContents().size() && info.position > 0) {
			final Content content = exp.getContents().get(info.position - 1);
			if (item.getItemId() == R.id.expcontentmenu_remove) {
				switchToEdit();
				Content c = null;
				try {
					c = exp.getContents().remove(info.position - 1);
					EBHelper.getSyncStorage().removeContent(c);
				} catch (DataException e) {
					Log.e(EditExpMuseFragment.class.getName(),
							"Error added content to deleted contents id: " + c != null ? c.getId() : null);
				}
				adapter.notifyDataSetChanged();
			} else if (item.getItemId() == R.id.expcontentmenu_comment) {
				DialogFragment newFragment = new EditNoteFragment();
				String text = content.getType() == ContentType.TEXT ? content.getValue() : content.getNote();
				newFragment.setArguments(EditNoteFragment.prepare(text, info.position - 1));
				newFragment.show(getActivity().getSupportFragmentManager(), "exp_content_note");
			} else {
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getSherlockActivity().getMenuInflater();
		inflater.inflate(R.menu.exp_content_list_menu, menu);
	}

	@Override
	public void onPosition(String txt) {
		if (txt != null) {
			exp.setAddress(txt);
			updateFooterTV(txt, null);
			switchToEdit();
		}
	}

	@Override
	public void onNote(String note, int idx) {
		if (idx == exp.getContents().size()) {
			// new text element
			Content c = new Content();
			c.setId(Utils.generateUID());
			c.setType(ContentType.TEXT);
			c.setTimestamp(System.currentTimeMillis());
			c.setValue(note);
			c.setLocalValue(note);
			switchToEdit();
			exp.getContents().add(0, c);
			adapter.notifyDataSetChanged();
		} else {
			Content oldElement = exp.getContents().get(idx);
			if (oldElement.getType() == ContentType.TEXT) {
				// update content
				String oldValue = oldElement.getValue();
				if (oldValue != null && !oldValue.equals(note) || oldValue == null && note != null) {
					switchToEdit();
					oldElement.setValue(note);
					oldElement.setLocalValue(note);
					adapter.notifyDataSetChanged();
				}
			} else {
				// update note
				String oldNote = oldElement.getNote();
				if (oldNote != null && !oldNote.equals(note) || oldNote == null && note != null) {
					switchToEdit();
					oldElement.setNote(note);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onRemoved(String id) {
		if (getActivity() instanceof CatchActivity) {
			getActivity().finish();
		} else {
			// src.markDeleted();
			getActivity().onBackPressed();
		}
	}

	@Override
	public void onCollectionsAssigned(String id, List<String> colls) {
		exp.setCollectionIds(colls);
		updateCollectionTV();
		switchToEdit();
	}

	@Override
	public void onCollectionSaved(ExpCollection coll) {
		exp.setCollectionIds(Collections.singletonList(coll.getId()));
		updateCollectionTV();
		switchToEdit();
	}

	private void openGrabDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_grab);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.grabdialog, null);
		ListView list = (ListView) v.findViewById(R.id.grabList);
		final String[] items = getResources().getStringArray(R.array.grabDialogArray);
		final String[] types = getResources().getStringArray(R.array.grabDialogTypesArray);
		final String[] labels = getResources().getStringArray(R.array.grabDialogLabelsArray);

		builder.setView(v);
		final AlertDialog dialog = builder.create();

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,
				items);
		list.setAdapter(arrayAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String typeString = types[position];
				// final String[] elems = typeString.split("\\|");
				// final String[] labelElems = labels[position].split("\\|");
				// assert labelElems.length == elems.length;
				// final Intent i = new Intent(getActivity(),
				// CatchActivity.class);
				// if (elems.length > 1) {
				// getDialog().hide();
				// AlertDialog.Builder builder = new
				// AlertDialog.Builder(getActivity());
				// builder.setItems(labelElems, new OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int which) {
				// startCapture(i, elems[which]);
				// dialog.dismiss();
				// dismiss();
				// }
				// });
				// AlertDialog dlg = builder.create();
				// dlg.setOwnerActivity(getActivity());
				// dlg.show();
				// } else {

				if (typeString.equals("TEXT")) {
					DialogFragment textFragment = new EditNoteFragment();
					textFragment.setArguments(EditNoteFragment.prepare("", exp.getContents().size()));
					textFragment.show(getActivity().getSupportFragmentManager(), "exp_content_note");
				} else {
					startCapture(typeString);
				}
				dialog.dismiss();
				// }
			}

		});

		dialog.show();
	}

}
