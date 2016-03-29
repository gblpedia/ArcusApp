package com.atid.app.rfid.view.base;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;

import android.widget.Spinner;

public abstract class ReadWriteMemoryActivity extends AccessActivity {

	protected static final int MAX_ADDRESS = 32;
	
	protected static final BankType DEFAULT_BANK = BankType.EPC;
	protected static final int DEFAULT_OFFSET = 2;
	protected static final int DEFAULT_LENGTH = 2;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------
	
	private Spinner spnBank;
	private Spinner spnOffset;

	private SpinnerValueAdapter adpBank;
	private SpinnerValueAdapter adpOffset;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public ReadWriteMemoryActivity() {
		super();
		TAG = ReadWriteMemoryActivity.class.getSimpleName();
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();
		
		// Initialize Bank Spinner
		spnBank = (Spinner)findViewById(R.id.bank);
		adpBank = new SpinnerValueAdapter(this, android.R.layout.simple_list_item_1);
		for (BankType item : BankType.values()) {
			adpBank.addItem(item.getValue(), item.toString());
		}
		spnBank.setAdapter(adpBank);
		spnBank.setOnItemSelectedListener(this);
		
		// Initialize Offset Spinner
		spnOffset = (Spinner)findViewById(R.id.offset);
		adpOffset = new SpinnerValueAdapter(this, android.R.layout.simple_list_item_1);
		for (int i = 0; i < MAX_ADDRESS; i++) {
			adpOffset.addItem(i, String.format(Locale.US, "%d word", i));
		}
		spnOffset.setAdapter(adpOffset);
		spnOffset.setOnItemSelectedListener(this);
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);
		if (mReader.getAction() == ActionState.Stop) {
			spnBank.setEnabled(enabled);
			spnOffset.setEnabled(enabled);
		} else {
			spnBank.setEnabled(false);
			spnOffset.setEnabled(false);
		}
	}
	
	protected BankType getBank() {
		int position = spnBank.getSelectedItemPosition();
		int value = adpBank.getValue(position);
		return BankType.valueOf(value);
	}
	
	protected void setBank(BankType bank) {
		int position = adpBank.indexOf(bank.getValue());
		spnBank.setSelection(position);
	}
	
	protected int getOffset() {
		int position = spnOffset.getSelectedItemPosition();
		return adpOffset.getValue(position);
	}
	
	protected void setOffset(int offset) {
		int position = adpOffset.indexOf(offset);
		spnOffset.setSelection(position);
	}
}
