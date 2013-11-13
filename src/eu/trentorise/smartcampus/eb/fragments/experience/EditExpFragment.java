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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
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
public class EditExpFragment extends SherlockFragment
		implements
		OnTagsSelectedListener,
		OnEditListener,
		ResultHandler,
		NoteHandler,
		eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback,
		eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback,
		CollectionSavedHandler, PositionHandler {

	private TextEditSwitch mTitleSwitch, mDescrSwitch;
	private Experience exp = null;
	private Experience src = null;

	private boolean editMode = false;

	private CaptureHelper mHelper = null;

	private ExpContentAdapter adapter = null;

	private View returnView;

	private static final String ARG_SRC = "src";
	public static final String ARG_EXP = "exp";
	public static final String ARG_VALUE = "value";

	public static final int ACCOUNT_CREATION = 10000;

	public static Bundle prepareArgs(Experience e, GrabbedContent content) {
		Bundle b = new Bundle();
		if (e != null)
			b.putSerializable(ARG_EXP, e);
		if (content != null)
			b.putSerializable(ARG_VALUE, content);
		return b;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new CaptureHelper(getSherlockActivity(), 10, this);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(ARG_EXP)) {
			exp = (Experience) savedInstanceState.getSerializable(ARG_EXP);
			src = (Experience) savedInstanceState.getSerializable(ARG_SRC);
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_EXP)) {
			src = (Experience) getArguments().getSerializable(ARG_EXP);
			src.copyTo(exp = new Experience());
		} else {
			exp = new Experience();
			src = exp;
		}

		if (savedInstanceState == null && getArguments() != null
				&& getArguments().containsKey(ARG_VALUE)) {
			appendContent((GrabbedContent) getArguments().getSerializable(
					ARG_VALUE));
		}

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("editMode")) {
			editMode = savedInstanceState.getBoolean("editMode");
		}

		if (exp.getContents() == null)
			exp.setContents(new ArrayList<Content>());
		setHasOptionsMenu(true);
	}

	private void appendContent(GrabbedContent value) {
		if (value.contentType() == ContentType.TEXT)
			return;

		Content c = value.toContent();
		c.setId(Utils.generateUID());
		c.setType(value.contentType());
		c.setTimestamp(System.currentTimeMillis());

		if (exp.getContents() == null)
			exp.setContents(new ArrayList<Content>());
		exp.getContents().add(0, c);

		exp.resetPreview();
		src.resetPreview();

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(ARG_EXP, exp);
		outState.putSerializable(ARG_SRC, src);
		outState.putBoolean("editMode", editMode);
	}

	@Override
	public void onPause() {
		super.onPause();
		exp.setTitle(mTitleSwitch.getValue());
		exp.setDescription(mDescrSwitch.getValue());
	}

	@Override
	public void onResume() {
		super.onResume();
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setTitle(
				R.string.title_expform);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		returnView = inflater.inflate(R.layout.exp_form, container, false);
		ListView list = (ListView) returnView.findViewById(R.id.exp_contents);
		registerForContextMenu(list);
		if (list.getFooterViewsCount() == 0) {
			View footer = getSherlockActivity().getLayoutInflater().inflate(
					R.layout.exp_form_footer, null);
			list.addFooterView(footer, null, false);
		}
		if (list.getHeaderViewsCount() == 0) {
			View header = getSherlockActivity().getLayoutInflater().inflate(
					R.layout.exp_form_header, null);
			list.addHeaderView(header, null, false);
			mTitleSwitch = new TextEditSwitch(returnView, R.id.title_switcher,
					R.id.title_tv, R.id.title, this);
			mTitleSwitch.setValue(exp.getTitle());
			mDescrSwitch = new TextEditSwitch(returnView, R.id.descr_switcher,
					R.id.description_tv, R.id.description, this);
			mDescrSwitch.setValue(exp.getDescription());
		}
		adapter = new ExpContentAdapter(getSherlockActivity(),
				R.layout.exp_contents_row, exp.getContents());
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position <= exp.getContents().size() && position > 0) {
					ContentRenderer.renderExternal(getActivity(), exp
							.getContents().get(position - 1));
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
			((TextView) returnView.findViewById(R.id.tags_tv)).setText(Concept
					.toSimpleString(exp.getTags()));
		}

		((TextView) returnView.findViewById(R.id.tags_tv))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TaggingDialogFragment taggingDialog = new TaggingDialogFragment();
						taggingDialog.setArguments(TaggingDialogFragment
								.prepare(Concept.convertToSS(exp.getTags())));
						taggingDialog.show(getActivity()
								.getSupportFragmentManager(), "tags");
					}
				});

		returnView.findViewById(R.id.place_box).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						DialogFragment textFragment = new EditPositionFragment();
						textFragment.setArguments(EditPositionFragment
								.prepare(exp.getAddress() == null ? "" : exp
										.getAddress()));
						textFragment.show(getActivity()
								.getSupportFragmentManager(), "exp_position");
					}
				});

		return returnView;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	private void updateFooterTV(String address, Long creationTime) {
		if (creationTime != null) {
			((TextView) returnView.findViewById(R.id.date_tv))
					.setText(Constants.DATE_FORMATTER.format(new Date(
							creationTime)));
		}
		if (address != null) {
			((TextView) returnView.findViewById(R.id.place_tv))
					.setText(address);
		}
	}

	private void updateCollectionTV() {
		TextView tv = (TextView) returnView.findViewById(R.id.collections_tv);
		if (exp.getCollectionIds() == null || exp.getCollectionIds().isEmpty()) {
			tv.setVisibility(View.GONE);
		} else {
			String txt = EBHelper.getUserPreference().collectionNames(
					exp.getCollectionIds());
			if (txt == null || txt.length() == 0) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setVisibility(View.VISIBLE);
				tv.setText(txt);
			}
		}
	}

	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		List<Concept> list = Concept.convertSS(suggestions);
		if (list != null)
			exp.setTags(list);
		((TextView) returnView.findViewById(R.id.tags_tv)).setText(Concept
				.toSimpleString(list));
		switchToEdit();
	}

	@Override
	public void onEdit() {
		editMode = true;
		getSherlockActivity().invalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			break;
		case R.id.expmenu_done:
		case R.id.expmenu_save:
			exp.setTitle(mTitleSwitch.getValue());
			exp.setDescription(mDescrSwitch.getValue());
			if (validate(exp)) {
				// TODO uncomment this to enable synchronization
//				try {
//					if (EBHelper.getConfiguration(EBHelper.CONF_SYNCHRO,
//							Boolean.class)
//							&& EBHelper.getConfiguration(
//									EBHelper.CONF_USER_ACCOUNT, String.class) == null) {
//						EBHelper.askUserAccount(this, ACCOUNT_CREATION, false);
//					} else {
						new SaveTask().execute();
//					}
//				} catch (DataException e) {
//					Log.e(EditExpFragment.class.getName(),
//							"Error creating filestorage account");
//				}
			}
			break;
		case R.id.expmenu_attach_audio:
			onCaptureOption(new String[] { CATCH_TYPES.AUDIO.toString() }, null);
			break;
		case R.id.expmenu_attach_camera_photo:
			onCaptureOption(
					new String[] { CATCH_TYPES.IMAGE_CAMERA.toString() }, null);
			break;
		case R.id.expmenu_attach_camera_video:
			onCaptureOption(
					new String[] { CATCH_TYPES.VIDEO_CAMERA.toString() }, null);
			break;
		case R.id.expmenu_attach_gallery_photo:
			onCaptureOption(
					new String[] { CATCH_TYPES.IMAGE_GALLERY.toString() }, null);
			break;
		case R.id.expmenu_attach_gallery_video:
			onCaptureOption(
					new String[] { CATCH_TYPES.VIDEO_GALLERY.toString() }, null);
			break;
		case R.id.expmenu_attach_qrcode:
			onCaptureOption(new String[] { CATCH_TYPES.QRCODE.toString() },
					null);
			break;
		case R.id.expmenu_attach_text:
			DialogFragment textFragment = new EditNoteFragment();
			textFragment.setArguments(EditNoteFragment.prepare("", exp
					.getContents().size()));
			textFragment.show(getActivity().getSupportFragmentManager(),
					"exp_content_note");
			break;
		case R.id.expmenu_remove:
			DialogFragment newFragment = new DeleteExperienceFragment();
			newFragment.setArguments(DeleteExperienceFragment.prepare(exp
					.getId()));
			newFragment.show(getActivity().getSupportFragmentManager(),
					"exp_delete");
			break;
		case R.id.expmenu_assign_collection:
			DialogFragment assignFragment = new AssignCollectionFragment();
			assignFragment.setArguments(AssignCollectionFragment.prepare(
					exp.getId(), exp.getCollectionIds()));
			assignFragment.show(getActivity().getSupportFragmentManager(),
					"exp_assign_colls");
			break;
		case R.id.expmenu_share:
			EBHelper.share(exp, getActivity());
			break;
//		case R.id.expmenu_map:
//		case R.id.expmenu_export:
//			Toast.makeText(getActivity(), R.string.not_implemented,
//					Toast.LENGTH_SHORT).show();
//			 // TODO
//			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
			Toast.makeText(getActivity(), R.string.alert_title_required,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (!this.equals(((DialogCallbackContainer) getActivity())
				.getNoteHandler())) {
			return;
		}

		menu.clear();
		if (exp == null || exp.getId() == null) {
			// new element menu
			getSherlockActivity().getSupportMenuInflater().inflate(
					R.menu.exp_menu_new, menu);
		} else if (!editMode) {
			// view mode
			getSherlockActivity().getSupportMenuInflater().inflate(
					R.menu.exp_menu, menu);
			MenuItem item = menu.findItem(R.id.expmenu_share);
			if (item != null)
				item.setEnabled(exp.getEntityId() != null).setVisible(
						exp.getEntityId() != null);
		} else {
			// edit mode
			getSherlockActivity().getSupportMenuInflater().inflate(
					R.menu.exp_menu_edit, menu);
			MenuItem item = menu.findItem(R.id.expmenu_share);
			if (item != null)
				item.setEnabled(exp.getEntityId() != null).setVisible(
						exp.getEntityId() != null);
		}
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACCOUNT_CREATION) {
			try {
				if (resultCode == Activity.RESULT_OK) {
					String accountId = data
							.getStringExtra(FilestorageAccountActivity.EXTRA_USER_ACCOUNT_ID);
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, true,
							Boolean.class);
					EBHelper.saveConfiguration(EBHelper.CONF_USER_ACCOUNT,
							accountId, String.class);
				} else {
					EBHelper.saveConfiguration(EBHelper.CONF_SYNCHRO, false,
							Boolean.class);
				}
			} catch (DataException e) {
				Log.e(EditExpFragment.class.getName(),
						"Error saving configuration: "
								+ EBHelper.CONF_USER_ACCOUNT);
			}
			new SaveTask().execute();
		}

		mHelper.onCaptureResult(requestCode, resultCode, data);
	}

	@Override
	public void onResult(GrabbedContent value) {
		appendContent(value);
		switchToEdit();
	}

	@Override
	public void onCancel() {
	}

	protected void startCapture(String string) {
		mHelper.startCapture(CATCH_TYPES.valueOf(string));
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
				exp.setLocation(new double[] { result.getLatitude(),
						result.getLongitude() });
				updateFooterTV(addrString, System.currentTimeMillis());
			}
		}
	}

	private class SaveTask extends SCAsyncTask<Void, Void, Experience> {

		public SaveTask() {
			super(getSherlockActivity(),
					new AbstractAsyncTaskProcessor<Void, Experience>(
							getSherlockActivity()) {
						@Override
						public Experience performAction(Void... params)
								throws SecurityException, ConnectionException,
								Exception {
							exp.setUpdateTime(System.currentTimeMillis());
							String expId = exp.getId();
							if (expId == null) {
								Address a = Utils
										.getCurrentPlace(getSherlockActivity());
								if (a == null && exp.getAddress() == null)
									return null;
								if (exp.getAddress() == null) {
									exp.setAddress(Utils
											.getShortAddressString(a));
								}
								if (a != null) {
									exp.setLocation(new double[] {
											a.getLatitude(), a.getLongitude() });
								}
								exp.setCreationTime(exp.getUpdateTime());
							}

							if (expId == null) {
								// save locally
								exp = EBHelper.saveExperience(getActivity(), exp, false);
								// if synchronized, force server sync
								if (EBHelper.isSynchronizationActive()) {
									// sync data with server without files sync
									EBHelper
											.getSyncStorage()
											.synchroFile(
													EBHelper.getAuthToken(), 
													false,
													GlobalConfig
															.getAppUrl(getActivity()
																	.getApplicationContext()),
													eu.trentorise.smartcampus.eb.custom.data.Constants.SYNC_SERVICE);
									exp = EBHelper.getSyncStorage().getObjectById(exp.getId(), Experience.class);
									// touch object to enable file synchronization
									EBHelper.saveExperience(getActivity(), exp, false);
									// call async synchronization of files
									EBHelper.synchronize(true);
//									if (data.getUpdated()
//											.get(Experience.class
//													.getCanonicalName()) != null) {
//										for (Object o : data.getUpdated().get(
//												Experience.class
//														.getCanonicalName())) {
//											Experience updatedExp = eu.trentorise.smartcampus.android.common.Utils
//													.convertObjectToData(
//															Experience.class, o);
//											if (updatedExp.getId().equals(
//													exp.getId())) {
//												return updatedExp;
//											}
//										}
//									}
								}
							} else {
								exp = EBHelper.saveExperience(getActivity(),
										exp, true);
							}
							return exp;
						}

						@Override
						public void handleResult(Experience result) {
							if (result != null) {
								exp = result;
								if (exp.getId() != null)
									switchToView();
								updateFooterTV(exp.getAddress(),
										exp.getCreationTime());
								exp.copyTo(src);
							} else {
								Toast.makeText(getSherlockActivity(),
										R.string.msg_location_undefined,
										Toast.LENGTH_SHORT).show();
							}
						}

					});
		}
	}

	@Override
	public void onDestroy() {
		if (adapter != null)
			adapter.release();
		super.onDestroy();
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (!this.equals(((DialogCallbackContainer) getActivity())
				.getNoteHandler())) {
			return false;
		}

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (info.position <= exp.getContents().size() && info.position > 0) {
			final Content content = exp.getContents().get(info.position - 1);
			switch (item.getItemId()) {
			case R.id.expcontentmenu_remove:
				switchToEdit();
				Content c = null;
				try {
					c = exp.getContents().remove(info.position - 1);
					EBHelper.getSyncStorage().removeContent(c);
				} catch (DataException e) {
					Log.e(EditExpFragment.class.getName(),
							"Error added content to deleted contents id: " + c != null ? c
									.getId() : null);
				}
				adapter.notifyDataSetChanged();
				break;
			case R.id.expcontentmenu_comment:
				DialogFragment newFragment = new EditNoteFragment();
				String text = content.getType() == ContentType.TEXT ? content
						.getValue() : content.getNote();
				newFragment.setArguments(EditNoteFragment.prepare(text,
						info.position - 1));
				newFragment.show(getActivity().getSupportFragmentManager(),
						"exp_content_note");
				break;
			default:
				break;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getSherlockActivity()
				.getMenuInflater();
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
				if (oldValue != null && !oldValue.equals(note)
						|| oldValue == null && note != null) {
					switchToEdit();
					oldElement.setValue(note);
					oldElement.setLocalValue(note);
					adapter.notifyDataSetChanged();
				}
			} else {
				// update note
				String oldNote = oldElement.getNote();
				if (oldNote != null && !oldNote.equals(note) || oldNote == null
						&& note != null) {
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
			src.markDeleted();
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

}
