package com.atid.app.rfid.view;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.dialog.PasswordDialog;
import com.atid.app.rfid.view.base.AccessActivity;
import com.atid.app.rfid.widgets.LockTypeSpinner;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.LockParam;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;

import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;

public class LockMemoryActivity extends AccessActivity implements
		OnClickListener, OnItemSelectedListener {
	
	private static final int OFFSET_ACCESS_PASSWORD = 2;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private LockTypeSpinner spnKillPassword;
	private LockTypeSpinner spnAccessPassword;
	private LockTypeSpinner spnEpc;
	private LockTypeSpinner spnTid;
	private LockTypeSpinner spnUser;
	private Button btnAction;
	private Button btnSetPassword;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public LockMemoryActivity() {
		super();

		TAG = LockMemoryActivity.class.getSimpleName();
		mView = R.layout.activity_lock_memory;
	}

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.set_password) {
			setAccessPassword();
		}
	}

	// ------------------------------------------------------------------------
	// Reader Control Methods
	// ------------------------------------------------------------------------

	// Start Action
	@Override
	protected void startAction() {

		ResultCode res;
		TagType tagType = getTagType();
		LockParam param = null;
		String password = getPassword();

		clear();
		enableWidgets(false);

		switch (tagType) {
		case Tag6C:
			param = getLockParam();
			if ((res = mReader.lock6c(param, password)) != ResultCode.NoError) {
				Log.e(TAG, String.format(Locale.US, 
						"ERROR. startAction() - Failed to lock 6C tag [%s]",
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

		// Initialize KillPassword Spinner
		spnKillPassword = new LockTypeSpinner(this, R.id.kill_password);

		// Initialize AccessPassword Spinner
		spnAccessPassword = new LockTypeSpinner(this, R.id.access_password);

		// Initialize EPC Spinner
		spnEpc = new LockTypeSpinner(this, R.id.epc);

		// Initialize TID Spinner
		spnTid = new LockTypeSpinner(this, R.id.tid);

		// Initialize User Spinner
		spnUser = new LockTypeSpinner(this, R.id.user);

		// Initialize Action Button
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);

		// Initialize Set Password Button
		btnSetPassword = (Button) findViewById(R.id.set_password);
		btnSetPassword.setOnClickListener(this);
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);

		if (mReader.getAction() == ActionState.Stop) {
			spnKillPassword.setEnabled(enabled);
			spnAccessPassword.setEnabled(enabled);
			spnEpc.setEnabled(enabled);
			spnTid.setEnabled(enabled);
			spnUser.setEnabled(enabled);
			btnAction.setText(R.string.action_lock);
			btnSetPassword.setEnabled(enabled);
		} else {
			spnKillPassword.setEnabled(false);
			spnAccessPassword.setEnabled(false);
			spnEpc.setEnabled(false);
			spnTid.setEnabled(false);
			spnUser.setEnabled(false);
			btnAction.setText(R.string.action_stop);
			btnSetPassword.setEnabled(false);
		}
		btnAction.setEnabled(enabled);
	}

	// ------------------------------------------------------------------------
	// Internal Helper Methods
	// ------------------------------------------------------------------------

	// Set Access Password
	private void setAccessPassword() {
		enableWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this,
				R.string.set_access_password_title);
		dlg.setResultListener(new PasswordDialog.IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {

				clear();

				ResultCode res;
				String data = dlg.getPassword();
				String password = getPassword();
				int time = getOperationTime();
				try {
					mReader.setOperationTime(time);
				} catch (ATRfidReaderException e) {
					Log.e(TAG,
							String.format(Locale.US, 
									"ERROR. setAccessPassword() - Failed to set operation time(%d)",
									time), e);
				}
				if ((res = mReader.writeMemory6c(BankType.Reserved,
						OFFSET_ACCESS_PASSWORD, data, password)) != ResultCode.NoError) {
					Log.e(TAG,
							String.format(Locale.US, 
									"ERROR. setAccessPassword() - Failed to write memory {[%s], [%s]} - [%s]",
									data, password, res));
					return;
				}

				enableWidgets(false);

				Log.i(TAG, "INFO. setAccessPassword()");
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				enableWidgets(true);
			}
			
		});
		dlg.show();
	}

	private LockParam getLockParam() {
		return new LockParam(spnKillPassword.getLockType(),
				spnAccessPassword.getLockType(), spnEpc.getLockType(),
				spnTid.getLockType(), spnUser.getLockType());
	}
}