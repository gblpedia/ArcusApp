package com.atid.app.rfid.view.base;

import au.com.adilamtech.app.arcus.R;

import java.util.Locale;

import com.atid.app.rfid.GlobalInfo;
import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.app.rfid.util.SoundPlay;
import com.atid.app.rfid.view.InventoryActivity;
import com.atid.app.rfid.view.SelectionMask6cActivity;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public abstract class ActionActivity extends ReaderActivity implements OnClickListener, OnItemSelectedListener {

	private static final int MAX_POWER_LEVEL = 300;

	private static final int SELECTION_MASK_VIEW = 6;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private Spinner spnPower;
	private Spinner spnTagType;
	private EditText edtOperationTime;

	private Button btnClear;
	private Button btnMask;

	private View mTagTypeSplitor;
	private View mTagTypeLayout;

	private SpinnerValueAdapter adpPower;
	private SpinnerValueAdapter adpTagType;

	private RangeValue mPowerRange;
	private int mPowerLevel;
	private int mOperationTime;

	private SoundPlay mSound;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public ActionActivity() {
		super();

		TAG = ActionActivity.class.getSimpleName();

		mPowerRange = null;
		mPowerLevel = MAX_POWER_LEVEL;
		mOperationTime = 0;
	}

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSound = new SoundPlay(this);

		// Clear Activity
		clear();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		enableWidgets(true);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//
		if ((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) && event.getRepeatCount() <= 0
				&& mReader.getAction() == ActionState.Stop && mReader.getState() == ConnectionState.Connected) {

			Log.i(TAG, String.format(Locale.US, "INFO. onKeyDown(%d, %d)", keyCode, event.getAction()));

			startAction();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) && event.getRepeatCount() <= 0
				&& mReader.getAction() != ActionState.Stop && mReader.getState() == ConnectionState.Connected) {

			Log.i(TAG, String.format(Locale.US, "INFO. onKeyUp(%d, %d)", keyCode, event.getAction()));

			stopAction();

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.clear:
			clear();
			break;
		case R.id.mask:
			Intent intent = null;
			enableWidgets(false);
			intent = new Intent(this, SelectionMask6cActivity.class);
			startActivityForResult(intent, SELECTION_MASK_VIEW);
			break;
		case R.id.action:
			enableWidgets(false);
			if (mReader.getAction() == ActionState.Stop) {
				startAction();
			} else {
				stopAction();
			}
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.power_gain:
			try {
				int power = adpPower.getValue(position);
				mReader.setPower(power);
			} catch (ATRfidReaderException e) {
				Log.e(TAG, String.format(Locale.US, "ERROR. onItemSelected(%d)", position));
			}
			break;
		case R.id.tag_type:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	// Clear Widgets
	protected abstract void clear();

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {

		// Initialize Power Gain
		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerValueAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);
		spnPower.setOnItemSelectedListener(this);

		// Initialize Operation Time
		edtOperationTime = (EditText) findViewById(R.id.operation_time);

		// Initialize Tag Type
		spnTagType = (Spinner) findViewById(R.id.tag_type);
		adpTagType = new SpinnerValueAdapter(this, android.R.layout.simple_list_item_1);
		spnTagType.setAdapter(adpTagType);

		mTagTypeSplitor = findViewById(R.id.tag_type_splitor);
		mTagTypeLayout = findViewById(R.id.tag_type_layout);

		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);
		btnMask = (Button) findViewById(R.id.mask);
		btnMask.setOnClickListener(this);
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {

		if (mReader.getAction() == ActionState.Stop) {
			spnPower.setEnabled(enabled);
			edtOperationTime.setEnabled(enabled);
			spnTagType.setEnabled(enabled);
			btnClear.setEnabled(enabled);
			btnMask.setEnabled(enabled);
		} else {
			spnPower.setEnabled(false);
			edtOperationTime.setEnabled(false);
			spnTagType.setEnabled(false);
			btnClear.setEnabled(false);
			btnMask.setEnabled(false);
		}
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		// Get Power Range
		try {
			mPowerRange = mReader.getPowerRange();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get power range [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Power Range : %d, %d]", mPowerRange.getMin(),
				mPowerRange.getMax()));

		// Get Power Level
		try {
			mPowerLevel = mReader.getPower();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get power level [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Power Level : %d]", mPowerLevel));

		// Get Operation Time
		try {
			mOperationTime = mReader.getOperationTime();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get operation time [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader(, args) - [Operation Time : %d]", mOperationTime));
	}

	// Activated Reader
	@Override
	protected void activateReader() {

		// Fill Power Spinner
		/*for (int i = mPowerRange.getMax(); i >= mPowerRange.getMin(); i -= 10) {
			adpPower.addItem(i, String.format(Locale.US, "%.1f dBm", ((double) i / 10.0)));
		}*/
		adpPower.addItem(300, getResources().getString(R.string.label_long_range));
		adpPower.addItem(250, getResources().getString(R.string.label_medium_range));
		adpPower.addItem(200, getResources().getString(R.string.label_short_range));

		adpPower.notifyDataSetChanged();

		// Set Power Level
		setPowerLevel(mPowerLevel);

		// Set Operation Time
		setOperationTime(mOperationTime);

		// Tag Type
		for (TagType item : TagType.values()) {
			adpTagType.addItem((int) item.getCode(), item.toString());
		}
		adpTagType.notifyDataSetChanged();
		switch (mReader.getModuleType()) {
		case I900MA:
			visibleTagType(true);
			setTagType(GlobalInfo.getTagType());
			break;
		case AT9200P_1:
		case ATX00S_1:
			visibleTagType(false);
			setTagType(TagType.Tag6C);
			break;
		}
	}

	// ------------------------------------------------------------------------
	// Override Widgets Access Methods
	// ------------------------------------------------------------------------

	protected int getPowerLevel() {
		int position = spnPower.getSelectedItemPosition();
		return adpPower.getValue(position);
	}

	protected void setPowerLevel(int power) {
		int position = adpPower.indexOf(power);
		spnPower.setSelection(position);
	}

	protected TagType getTagType() {
		int position = spnTagType.getSelectedItemPosition();
		int value = adpTagType.getValue(position);
		TagType type = TagType.valueOf((char) value);
		return type;
	}

	protected void setTagType(TagType type) {
		int position = adpTagType.indexOf(type.getCode());
		spnTagType.setSelection(position);
	}

	protected int getOperationTime() {
		String value = edtOperationTime.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(value);
		} catch (Exception e) {
			Log.e(TAG,
					String.format(Locale.US, "ERROR. getOperationTime() - Failed to parse operation time [%s]", value),
					e);
			time = 0;
		}
		return time;
	}

	protected void setOperationTime(int time) {
		edtOperationTime.setText(String.format(Locale.US, "%d", time));
	}

	// Start Action
	protected abstract void startAction();

	// Stop Action
	protected void stopAction() {

		ResultCode res;

		enableWidgets(false);

		if ((res = mReader.stop()) != ResultCode.NoError) {
			Log.e(TAG, String.format(Locale.US, "ERROR. stopAction() - Failed to stop operation [%s]", res));
			enableWidgets(true);
			return;
		}

		Log.i(TAG, "INFO. stopAction()");
	}

	// ------------------------------------------------------------------------
	// Intenal Widgets Control Methods
	// ------------------------------------------------------------------------

	private void visibleTagType(boolean visible) {
		int visiblity = visible ? View.VISIBLE : View.GONE;
		mTagTypeSplitor.setVisibility(visiblity);
		mTagTypeLayout.setVisibility(visiblity);
	}

	protected void playSuccess() {
		mSound.playSuccess();
	}

	protected void playFail() {
		mSound.playFail();
	}
}
