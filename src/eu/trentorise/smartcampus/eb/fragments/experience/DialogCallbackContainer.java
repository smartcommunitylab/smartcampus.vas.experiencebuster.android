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

import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.eb.fragments.NewCollectionDialogFragment.CollectionSavedHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.AssignCollectionFragment.AssignCollectionsCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.DeleteExperienceFragment.RemoveCallback;
import eu.trentorise.smartcampus.eb.fragments.experience.EditPositionFragment.PositionHandler;
import eu.trentorise.smartcampus.eb.fragments.experience.EditNoteFragment.NoteHandler;

public interface DialogCallbackContainer {

	CollectionSavedHandler getCollectionSavedHandler();
	AssignCollectionsCallback getAssignCollectionsCallback();
	RemoveCallback getRemoveCallback();
	NoteHandler getNoteHandler();
	OnTagsSelectedListener getTagListener();
	PositionHandler getPositionHandler();
	
//	String getCurrentId();
}
