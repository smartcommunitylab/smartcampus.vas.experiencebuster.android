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
package eu.trentorise.smartcampus.eb.custom.capture.content;

import eu.trentorise.smartcampus.eb.custom.capture.GrabbedContent;
import eu.trentorise.smartcampus.eb.model.Content;
import eu.trentorise.smartcampus.eb.model.ContentType;
import eu.trentorise.smartcampus.eb.model.NearMeObject;

public class ObjectContent implements GrabbedContent {
	private static final long serialVersionUID = 7114848425178997996L;

	private NearMeObject object;
	
	public ObjectContent(NearMeObject object) {
		super();
		this.object = object;
	}

	private String localValue() {
		String res = "<b>"+object.getTitle()+"</b>";
		if (object.getDescription() != null) res += "<br/>"+object.getDescription();
		return res;
	}

	private String value() {
		return object.getId();
	}

	@Override
	public ContentType contentType() {
		return ContentType.OBJECT;
	}

	@Override
	public Content toContent() {
		Content c = new Content();
		c.setEntityId(object.getEntityId());
		c.setEntityType(object.getEntityType());
		c.setLocalValue(localValue());
		c.setValue(value());
		return c;
	}
	
	
}
