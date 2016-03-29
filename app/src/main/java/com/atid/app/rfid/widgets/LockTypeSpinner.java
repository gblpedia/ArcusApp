package com.atid.app.rfid.widgets;

import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.lib.dev.rfid.type.LockType;

import android.app.Activity;
import android.widget.Spinner;

public class LockTypeSpinner {

	private Spinner mSpinner;
	private SpinnerValueAdapter mAdapter;
	
	public LockTypeSpinner(Activity activity, int resId) {
		mSpinner = (Spinner) activity.findViewById(resId);
		mAdapter = new SpinnerValueAdapter(activity,
				android.R.layout.simple_list_item_1);
		for (LockType item : LockType.values())
			mAdapter.addItem(item.getCode(), item.toString());
		mSpinner.setAdapter(mAdapter);
	}
	
	public void setEnabled(boolean enabled) {
		mSpinner.setEnabled(enabled);
	}
	
	public LockType getLockType() {
		int position = mSpinner.getSelectedItemPosition();
		int value = mAdapter.getValue(position);
		return LockType.valueOf(value);
	}
	
	public void setLockType(LockType type) {
		int position = mAdapter.indexOf(type.getCode());
		mSpinner.setSelection(position);
	}
}
