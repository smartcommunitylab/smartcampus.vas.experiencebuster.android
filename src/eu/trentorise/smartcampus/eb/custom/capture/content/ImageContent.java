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

public class ImageContent implements GrabbedContent {
	private static final long serialVersionUID = 7114848425178997996L;

	private String uri;
	
	public ImageContent(String uri) {
		super();
		this.uri = uri;
	}


	@Override
	public ContentType contentType() {
		return ContentType.PHOTO;
	}

	@Override
	public Content toContent() {
		Content c = new Content();
		c.setLocalValue(uri);
		c.setValue(uri);
		return c;
	}

}
