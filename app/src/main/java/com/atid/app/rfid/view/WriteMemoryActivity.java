package com.atid.app.rfid.view;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.view.base.AccessActivity;
import com.atid.app.rfid.view.base.ReadWriteMemoryActivity;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class WriteMemoryActivity extends ReadWriteMemoryActivity {

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private EditText edtData;
	private Button btnAction;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public WriteMemoryActivity() {
		super();

		TAG = WriteMemoryActivity.class.getSimpleName();
		mView = R.layout.activity_write_memory;
	}

	// ------------------------------------------------------------------------
	// Reader Control Methods
	// ------------------------------------------------------------------------

	// Start Action
	@Override
	protected void startAction() {
		
		ResultCode res;
		TagType tagType = getTagType();
		BankType bank;
		int offset = getOffset();
		String data = getData();
		String password = getPassword();
		
		clear();
		enableWidgets(false);
		
		switch(tagType) {
		case Tag6C:
			bank = getBank();
			if ((res = mReader.writeMemory6c(bank, offset, data, password)) != ResultCode.NoError) {
				Log.e(TAG,
						String.format(Locale.US, 
								"ERROR. startAction() - Failed to write memory 6C tag [%s]",
								res));
				enableWidgets(true);
				return;
			}
			break;
		case Tag6B:
			if ((res = mReader.writeMemory6b(offset, data)) != ResultCode.NoError) {
				Log.e(TAG,
						String.format(Locale.US, 
								"ERROR. startAction() - Failed to write memory 6B tag [%s]",
								res));
				enableWidgets(true);
				return;
			}
			break;
		}
		
		Log.i(TAG, "INFO. startAction()");
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();
		
		// Initialize Write Data EditText
		edtData = (EditText)findViewById(R.id.write_value);
		
		// Initialize Action Button
		btnAction = (Button)findViewById(R.id.action);
		btnAction.setOnClickListener(this);
		
		setBank(BankType.EPC);
		setOffset(2);
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);
		
		if (mReader.getAction() == ActionState.Stop) {
			edtData.setEnabled(enabled);
			btnAction.setText(R.string.action_write);
		} else {
			edtData.setEnabled(false);
			btnAction.setText(R.string.action_stop);
		}
		btnAction.setEnabled(enabled);
	}
	
	private String getData() {
		return edtData.getText().toString();
	}
	
	private void setData(String data) {
		edtData.setText(data);
	}
}
