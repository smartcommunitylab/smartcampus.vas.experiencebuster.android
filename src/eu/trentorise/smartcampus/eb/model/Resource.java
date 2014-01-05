/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.model;

import java.io.File;

/**
 * @author mirko perillo
 * 
 */
public class Resource {
	private File resourcefile;
	private byte[] content;
	private String contentType;
	private String name;
	private long size;

	public Resource() {
	}

	public Resource(File resourcefile, String contentType, String name) {
		super();
		this.resourcefile = resourcefile;
		this.contentType = contentType;
		this.name = name;
		size = resourcefile.length();
	}

	public Resource(byte[] content, String contentType, String name) {
		super();
		this.content = content;
		this.contentType = contentType;
		this.name = name;
		size = content.length;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getResourcefile() {
		return resourcefile;
	}

	public void setResourcefile(File resourcefile) {
		this.resourcefile = resourcefile;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
