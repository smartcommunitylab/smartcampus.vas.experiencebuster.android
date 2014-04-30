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
package eu.trentorise.smartcampus.eb;

import it.smartcampuslab.eb.R;
import net.sourceforge.zbar.SymbolSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.eb.custom.capture.QRCodeReaderHelper;
import eu.trentorise.smartcampus.eb.custom.capture.QRCodeResultHandler;

public class QRCodeActivity extends SherlockFragmentActivity {

	public static final String QR_DATA = "data";
	private QRCodeReaderHelper mHelper = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		startCapture();
	}

	private void startCapture() {
		setTitle(R.string.title_grab_qrcode);
        setContentView(R.layout.catch_qrcode);
        ViewGroup parent = (ViewGroup)findViewById(R.id.cameraPreview);
		mHelper = new QRCodeReaderHelper(this, parent, handler);
	}

    public void onPause() {
        super.onPause();
		if (mHelper != null) mHelper.release();
    }

	private QRCodeResultHandler handler = new QRCodeResultHandler() {
		@Override
		public void onQRCodeResults(SymbolSet result) {
			Intent data = new Intent();
			if (!result.isEmpty()) data.putExtra(QR_DATA, result.iterator().next().getData());
			setResult(RESULT_OK, data);
			finish();
		}
	};
}
