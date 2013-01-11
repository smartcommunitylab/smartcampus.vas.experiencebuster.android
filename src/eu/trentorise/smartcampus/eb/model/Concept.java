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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion.TYPE;

public class Concept implements Serializable {
	
	private static final long serialVersionUID = -4410031276807216428L;

	private Long id;
	private String name;
	private String description;
	private String summary;

	public Concept(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Concept() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public static List<Concept> convertSS(Collection<SemanticSuggestion> tags) {
		List<Concept> result = new ArrayList<Concept>();
		for (SemanticSuggestion ss : tags) {
			if (ss.getType() == TYPE.KEYWORD) {
				result.add(new Concept(null, ss.getName()));
			} else if (ss.getType() == TYPE.SEMANTIC) {
				Concept c = new Concept();
				c.setId(ss.getId());
				c.setName(ss.getName());
				c.setDescription(ss.getDescription());
				c.setSummary(ss.getSummary());
				result.add(c);
			}
		}
		return result;
	}

	public static Collection<SemanticSuggestion> convertToSS(List<Concept> tags) {
		if (tags == null) return Collections.emptyList();
		Collection<SemanticSuggestion> result = new ArrayList<SemanticSuggestion>();
		for (Concept c : tags) {
			SemanticSuggestion ss = new SemanticSuggestion();
			if (c.getId() == null) {
				ss.setType(TYPE.KEYWORD);
			} else {
				ss.setId(c.getId());
				ss.setDescription(c.getDescription());
				ss.setSummary(c.getSummary());
				ss.setType(TYPE.SEMANTIC);
			}
			ss.setName(c.getName());
			result.add(ss);
		}
		return result;
	}

	public static String toSimpleString(List<Concept> tags) {
		if (tags == null) return null;
		String content = "";
		for (Concept s : tags) {
			if (content.length() > 0) content += ", ";
			content += s.getName();
		}
		return content;
	}

}
