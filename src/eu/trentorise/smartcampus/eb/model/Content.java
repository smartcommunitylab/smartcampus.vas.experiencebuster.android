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
package eu.trentorise.smartcampus.eb.model;

import java.io.File;
import java.io.Serializable;

import android.os.Environment;

public class Content implements Serializable {
	private static final long serialVersionUID = 5647832073206951058L;

	private String id;
	private ContentType type;
	private String value;
	private String note;
	private String entityId;
	private String entityType;
	private long timestamp;
	private String localValue;
	private String thumbnail;

	private transient Object cache;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLocalValue() {
		return localValue;
	}

	public void setLocalValue(String localValue) {
		this.localValue = localValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == Content.class) {
			Content c = (Content) obj;
			return (c.getId() == null && id == null)
					|| (c.getId() != null && c.getId().equals(id));
		}
		return false;
	}

	public Object cached() {
		return cache;
	}

	public void cache(Object cache) {
		this.cache = cache;
	}

	public Content copy() {
		Content c = new Content();
		c.setEntityId(entityId);
		c.setEntityType(entityType);
		c.setId(id);
		c.setLocalValue(localValue);
		c.setNote(note);
		c.setTimestamp(timestamp);
		c.setType(type);
		c.setValue(value);
		c.cache(cache);
		return c;
	}

	public boolean isStorable() {
		return type == ContentType.FILE || type == ContentType.AUDIO
				|| type == ContentType.PHOTO || type == ContentType.VIDEO;
	}

	public boolean isUploaded() {
		return localValue != null && value != null && !localValue.equals(value);
	}

	public boolean permitThumbnail() {
		return type == ContentType.VIDEO || type == ContentType.PHOTO;
	}

	public String getRelativePath() {
		if (isStorable()) {
			return localValue.substring(localValue.indexOf("Pictures"));
		}
		return null;
	}

	public String getAbsolutePath() {
		if (isStorable()) {
			return new File(Environment.getExternalStorageDirectory(),
					getRelativePath()).getAbsolutePath();
		}
		return null;
	}

	public String getAbsolutePathThumbnail() {
		if (isStorable()) {
			if (thumbnail == null) {
				String path = getAbsolutePath();
				int extIndex = path.lastIndexOf(".");
				int pathIndex = path.lastIndexOf("/");
				String thumbName = path.substring(pathIndex + 1, extIndex - 1);
				String pathThumb = path.substring(0, pathIndex);
				String ext = path.substring(extIndex + 1);
				thumbnail = pathThumb + "/" + thumbName + "_thumb.jpg";
			}
			return thumbnail;
		}
		return null;
	}
}
