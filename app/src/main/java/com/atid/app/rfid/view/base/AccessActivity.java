package com.atid.app.rfid.view.base;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public abstract class AccessActivity extends ActionActivity {

	public static final String KEY_EPC = "epc";

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private TextView txtSelection;
	private ProgressBar progWait;
	private TextView txtMessage;
	private LinearLayout layoutBack;
	private EditText edtPassword;

	private String mEpc;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public AccessActivity() {
		super();

		TAG = AccessActivity.class.getSimpleName();
		mEpc = "";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mEpc = intent.getStringExtra(KEY_EPC);
		if (mEpc == null)
			mEpc = "";
		setSelection(mEpc);
	}

	// ------------------------------------------------------------------------
	// Reader Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {

		enableWidgets(true);

		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderActionchanged(%s)", action));
	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code,
			ActionState action, String epc, String data) {
		
		resultMessage(code);
		if (getEpc().equals("")) {
			setSelection(epc);
		}
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	@Override
	// Clear Widgets
	protected void clear() {
		txtSelection.setText(mEpc);
		showMessage("");
	}

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Initialize Selection TextView
		txtSelection = (TextView) findViewById(R.id.selection);

		// Initialize Wait ProgressBar
		progWait = (ProgressBar) findViewById(R.id.progress_bar);

		// Initialize Message TextView
		txtMessage = (TextView) findViewById(R.id.message);

		// Initialize Background LinearLayout
		layoutBack = (LinearLayout) findViewById(R.id.background);

		// Initialize Password EditText
		edtPassword = (EditText) findViewById(R.id.password);
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);
		if (mReader.getAction() == ActionState.Stop) {
			edtPassword.setEnabled(enabled);
		} else {
			edtPassword.setEnabled(false);
		}
	}

	// Activated Reader
	@Override
	protected void activateReader() {
		super.activateReader();

		enableWidgets(true);

		Log.i(TAG, "INFO. activateReader()");
	}

	// Display Message With ProgressBar
	protected void waitMessage(String msg) {
		progWait.setVisibility(View.VISIBLE);
		txtMessage.setText(msg);
		layoutBack.setBackgroundResource(R.color.message_background);
	}

	// Display Message Without ProgressBar
	protected void showMessage(String msg) {
		progWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		layoutBack.setBackgroundResource(R.color.message_background);
	}

	// Display ResultCode Without ProgressBar
	protected void resultMessage(ResultCode code) {
		String msg;
		int resId;

		if (code == ResultCode.NoError) {
			msg = "Success";
			resId = R.color.blue;
		} else {
			msg = code.toString();
			resId = R.color.red;
		}
		progWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		layoutBack.setBackgroundResource(resId);
	}

	// Get Access Password
	protected String getPassword() {
		return edtPassword.getText().toString();
	}

	// Set Access Password
	protected void setPassword(String password) {
		edtPassword.setText(password);
	}
	
	// Get EPC
	protected String getEpc() {
		return mEpc;
	}
	
	// Set EPC
	protected void setEpc(String epc) {
		mEpc = epc;
	}
	
	// Set Selection
	protected void setSelection(String epc) {
		txtSelection.setText(epc);
	}
}
