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
import java.util.Date;

import eu.trentorise.smartcampus.eb.Constants;

public class NearMeObject implements Serializable {
	private static final long serialVersionUID = -8598910587069997021L;

	private String id;
	private String address;

	private String description = null;
	private String title = null;
	private String source = null; // service 'source' of the object

	// semantic entity
	private String entityId = null;
	private String entityType = null;
	
	// only for user-created objects
	private String creatorId = null;
	private String creatorName = null;

	// categorization
	private String type = null;

	// common data
	private double[] location;
	private Long fromTime;
	private Long toTime;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double[] getLocation() {
		return location;
	}
	public void setLocation(double[] location) {
		this.location = location;
	}
	public Long getFromTime() {
		return fromTime;
	}
	public void setFromTime(Long fromTime) {
		this.fromTime = fromTime;
	}
	public Long getToTime() {
		return toTime;
	}
	public void setToTime(Long toTime) {
		this.toTime = toTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public CharSequence descriptionString() {
		String s = "";
		if (fromTime != null && fromTime > 0) {
			s += Constants.DATE_FORMATTER.format(new Date(fromTime));
			if (s.endsWith(" 00:00")) {
				s = s.substring(0,s.length()-6);
			}
		}
		if (address != null) {
			if (s.length() > 0) s += ", ";
			s+= address;
		}
		return s;
	}

}
