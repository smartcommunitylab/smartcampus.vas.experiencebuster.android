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

import java.io.Serializable;


public class Content implements Serializable{
	private static final long serialVersionUID = 5647832073206951058L;

	private String id;
	private ContentType type;
	private String value;
	private String note;
	private long entityId;
	private String entityType;
	private long timestamp;
	private String localValue;
	
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

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
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
}
