package com.atid.app.rfid.view;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.adapter.SelectionMask6cAdapter;
import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.app.rfid.dialog.SelectionMask6cDialog;
import com.atid.app.rfid.dialog.SelectionMask6cDialog.ISelectionMaskListener;
import com.atid.app.rfid.dialog.WaitDialog;
import com.atid.app.rfid.view.base.ReaderActivity;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.SelectionMask6c;
import com.atid.lib.dev.rfid.type.InventorySession;
import com.atid.lib.dev.rfid.type.InventoryTarget;
import com.atid.lib.util.SysUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;

public class SelectionMask6cActivity extends ReaderActivity implements
		OnClickListener, OnItemLongClickListener, ISelectionMaskListener,
		OnCheckedChangeListener {

	private static final int MAX_SELECTION_MASK_COUNT = 8;

	private CheckBox chkUseSelectionMask;
	private ListView lstMasks;
	private Spinner spnSession;
	private Spinner spnTarget;
	private Button btnSave;
	private Button btnCancel;

	private SelectionMask6cAdapter adpMasks;
	private SpinnerValueAdapter adpSession;
	private SpinnerValueAdapter adpTarget;

	private boolean mIsUseSelectionMask;
	private SelectionMask6c[] mMasks;
	private InventorySession mSession;
	private InventoryTarget mTarget;

	private SelectionMask6cDialog mMaskDialog;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public SelectionMask6cActivity() {
		super();

		TAG = SelectionMask6cActivity.class.getSimpleName();
		mView = R.layout.activity_selection_mask_6c;
		mIsUseSelectionMask = false;

		mMasks = new SelectionMask6c[MAX_SELECTION_MASK_COUNT];
		mSession = InventorySession.S0;
		mTarget = InventoryTarget.All;
	}

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		lstMasks.setEnabled(isChecked);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			WaitDialog.show(this, "Saving Selection Mask\r\nPlease Wait...");
			mIsUseSelectionMask = chkUseSelectionMask.isChecked();
			for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
				mMasks[i] = adpMasks.getItem(i);
			}
			mSession = InventorySession.valueOf(adpSession.getValue(spnSession
					.getSelectedItemPosition()));
			mTarget = InventoryTarget.valueOf(adpTarget.getValue(spnTarget
					.getSelectedItemPosition()));
			new Thread(mSaving).start();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mMaskDialog.show(position, adpMasks.getItem(position));
		return true;
	}

	@Override
	public void onOkClick(SelectionMask6cDialog dialog) {
		int position = dialog.getPosition();
		adpMasks.setItem(position, dialog.getSelectionMask());
		adpMasks.notifyDataSetChanged();
	}

	@Override
	public void onCancelClick(SelectionMask6cDialog dialog) {
	}

	public Runnable mSaving = new Runnable() {

		@Override
		public void run() {

			saveSelectionMask();
			
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WaitDialog.hide();
					finish();
				}

			});
		}

	};

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		chkUseSelectionMask = (CheckBox) findViewById(R.id.use_selection_mask);
		chkUseSelectionMask.setChecked(mIsUseSelectionMask);
		chkUseSelectionMask.setOnCheckedChangeListener(this);

		lstMasks = (ListView) findViewById(R.id.mask_list);
		adpMasks = new SelectionMask6cAdapter(this);
		lstMasks.setAdapter(adpMasks);
		lstMasks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lstMasks.setOnItemLongClickListener(this);

		spnSession = (Spinner) findViewById(R.id.session);
		adpSession = new SpinnerValueAdapter(this,
				android.R.layout.simple_list_item_1);
		spnSession.setAdapter(adpSession);

		spnTarget = (Spinner) findViewById(R.id.target);
		adpTarget = new SpinnerValueAdapter(this,
				android.R.layout.simple_list_item_1);
		spnTarget.setAdapter(adpTarget);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);

		// Initialzie Session
		for (InventorySession item : InventorySession.values()) {
			adpSession.addItem(item.getValue(), item.toString());
		}
		adpSession.notifyDataSetChanged();
		spnSession.setSelection(adpSession.indexOf(mSession.getValue()));

		// Initialize Target
		for (InventoryTarget item : InventoryTarget.values()) {
			adpTarget.addItem(item.getValue(), item.toString());
		}
		adpTarget.notifyDataSetChanged();
		spnTarget.setSelection(adpTarget.indexOf(mTarget.getValue()));
		
		Log.i(TAG, "INFO. initWidgets()");
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		chkUseSelectionMask.setEnabled(enabled);
		lstMasks.setEnabled(enabled && chkUseSelectionMask.isChecked());
		spnSession.setEnabled(enabled);
		spnTarget.setEnabled(enabled);
		btnSave.setEnabled(enabled);
		btnCancel.setEnabled(enabled);
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		try {
			mIsUseSelectionMask = mReader.getUseSelectionMask();
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. initReader() - Failed to get use selection mask [%s]",
							e.getCode()), e);
		}
		for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
			try {
				mMasks[i] = mReader.getSelectionMask6c(i);
			} catch (ATRfidReaderException e) {
				Log.e(TAG,
						String.format(Locale.US, 
								"ERROR. initReader() - Failed to get selection mask {%d} [%s]",
								i, e.getCode()), e);
			}
			SysUtil.sleep(10);
		}

		try {
			mSession = mReader.getInventorySession();
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. initReader() - Failed to get inventory session [%s]",
							e.getCode()), e);
		}
		try {
			mTarget = mReader.getInventoryTarget();
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. initReader() - Failed to get inventory target [%s]",
							e.getCode()), e);
		}

		Log.i(TAG, "INFO initReader()");
	}

	// Activated Reader
	@Override
	protected void activateReader() {

		mMaskDialog = new SelectionMask6cDialog(this, this);

		chkUseSelectionMask.setChecked(mIsUseSelectionMask);
		lstMasks.setEnabled(mIsUseSelectionMask);
		for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
			adpMasks.setItem(i, mMasks[i]);
		}
		adpMasks.notifyDataSetChanged();
		spnSession.setSelection(adpSession.indexOf(mSession
				.getValue()));
		spnTarget
				.setSelection(adpTarget.indexOf(mTarget.getValue()));

		enableWidgets(true);
		WaitDialog.hide();

		Log.i(TAG, "INFO. activateReader()");
	}

	// Save Selection Mask
	private void saveSelectionMask() {
		SelectionMask6c mask = null;

		try {
			mReader.setUseSelectionMask(mIsUseSelectionMask);
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. saveSelectionMask() - Failed to set use selection mask {%s} [%s]",
							mIsUseSelectionMask, e.getCode()), e);
		}

		for (int i = 0; i < MAX_SELECTION_MASK_COUNT; i++) {
			try {
				mReader.setSelectionMask6c(i, mMasks[i]);
			} catch (ATRfidReaderException e) {
				Log.e(TAG,
						String.format(Locale.US, 
								"ERROR. saveSelectionMask() - Failed to set selection mask {%d, [%s]} [%s]",
								i, mask, e.getCode()), e);
			}
		}

		try {
			mReader.setInventorySession(mSession);
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. saveSelectionMask() - Failed to set inventory session {%s} [%s]",
							mSession, e.getCode()), e);
		}
		try {
			mReader.setInventoryTarget(mTarget);
		} catch (ATRfidReaderException e) {
			Log.e(TAG,
					String.format(Locale.US, 
							"ERROR. saveSelectionMask() - Failed to set inventory target {%s} [%s]",
							mTarget, e.getCode()), e);
		}
	}
}
