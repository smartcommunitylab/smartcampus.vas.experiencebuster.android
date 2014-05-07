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
package eu.trentorise.smartcampus.eb.custom;

import java.util.Calendar;

import android.content.Context;
import android.net.ConnectivityManager;

public class EBUtils {

	public static Calendar millis2cal(long milliseconds, boolean clearTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(milliseconds);
		if (clearTime) {
			cal.clear(Calendar.HOUR);
			cal.clear(Calendar.MINUTE);
			cal.clear(Calendar.SECOND);
			cal.clear(Calendar.MILLISECOND);
		}
		return cal;
	}
	
	public static boolean isConnected(Context ctx){
		ConnectivityManager conMgr =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (conMgr.getActiveNetworkInfo()!=null && conMgr.getActiveNetworkInfo().isConnectedOrConnecting());
	}

}
