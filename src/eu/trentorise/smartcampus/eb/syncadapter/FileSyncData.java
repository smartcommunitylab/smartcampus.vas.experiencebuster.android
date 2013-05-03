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
package eu.trentorise.smartcampus.eb.syncadapter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author mirko perillo
 * 
 */
public class FileSyncData implements Serializable {
	private static final long serialVersionUID = 6150604524280819190L;

	private long version;
	private Map<String, List<Object>> updated;
	private Map<String, List<String>> deleted;

	public FileSyncData() {
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Map<String, List<Object>> getUpdated() {
		return updated;
	}

	public void setUpdated(Map<String, List<Object>> updated) {
		this.updated = updated;
	}

	public Map<String, List<String>> getDeleted() {
		return deleted;
	}

	public void setDeleted(Map<String, List<String>> deleted) {
		this.deleted = deleted;
	}

}
