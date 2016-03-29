package com.atid.app.rfid.dialog;

import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.app.rfid.util.StringUtil;
import com.atid.lib.dev.rfid.param.SelectionMask6c;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.MaskActionType;
import com.atid.lib.dev.rfid.type.MaskTargetType;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class SelectionMask6cDialog implements OnClickListener, OnCancelListener {

	private static final String TAG = SelectionMask6cDialog.class.getSimpleName();
	
	private static final int NIBBLE_UNIT = 4;
	
	private Spinner spnTarget;
	private Spinner spnAction;
	private Spinner spnBank;
	private EditText edtOffset;
	private EditText edtMask;
	private EditText edtLength;

	private SpinnerValueAdapter adpTarget;
	private SpinnerValueAdapter adpAction;
	private SpinnerValueAdapter adpBank;

	private ISelectionMaskListener mListener;
	
	private Dialog mDialog; 
	
	private int mPosition;
	private boolean mIsUsed;
	
	public SelectionMask6cDialog(Context context, ISelectionMaskListener listener) {

		mListener = listener;
		mPosition = -1;
		mIsUsed = false;
		
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_selection_mask_6c, null);
		// Target
		spnTarget = (Spinner) root.findViewById(R.id.mask_target);
		adpTarget = new SpinnerValueAdapter(context, R.layout.item_spinner_dialog,
				R.layout.item_dialog_list);
		for (MaskTargetType item : MaskTargetType.values()) {
			adpTarget.addItem(item.getValue(), item.toString());
		}
		spnTarget.setAdapter(adpTarget);
		spnTarget.setSelection(adpTarget.indexOf(MaskTargetType.SL.getValue()));
		// Action
		spnAction = (Spinner) root.findViewById(R.id.mask_action);
		adpAction = new SpinnerValueAdapter(context, R.layout.item_spinner_dialog,
				R.layout.item_dialog_list);
		for (MaskActionType item : MaskActionType.values()) {
			adpAction.addItem(item.getValue(), StringUtil.toString(item));
		}
		spnAction.setAdapter(adpAction);
		spnAction.setSelection(adpAction.indexOf(MaskActionType.Assert_Deassert.getValue()));
		// Bank
		spnBank = (Spinner) root.findViewById(R.id.mask_bank);
		adpBank = new SpinnerValueAdapter(context, R.layout.item_spinner_dialog,
				R.layout.item_dialog_list);
		for (BankType item : BankType.values()) {
			adpBank.addItem(item.getValue(), item.toString());
		}
		spnBank.setAdapter(adpBank);
		// Offset
		edtOffset = (EditText) root.findViewById(R.id.mask_offset);
		edtOffset.setText("16");
		// Mask
		edtMask = (EditText) root.findViewById(R.id.mask_value);
		edtMask.addTextChangedListener(maskWatcher);
		// Length
		edtLength = (EditText) root.findViewById(R.id.mask_length);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.mask_item_dialog);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, this);
		builder.setNegativeButton(R.string.action_cancel, this);
		
		mDialog = builder.create();
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(this);
	}
	
	public interface ISelectionMaskListener {
		void onOkClick(SelectionMask6cDialog dialog);
		void onCancelClick(SelectionMask6cDialog dialog);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			if (mListener != null)
				mListener.onOkClick(this);
			break;
		case Dialog.BUTTON_NEGATIVE:
			if (mListener != null)
				mListener.onCancelClick(this);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mListener != null)
			mListener.onCancelClick(this);
	}

	public int getPosition() {
		return mPosition;
	}
	
	public SelectionMask6c getSelectionMask() {
		
		SelectionMask6c mask = new SelectionMask6c();
		
		mask.setUsed(mIsUsed);
		mask.setTarget(MaskTargetType.valueOf(adpTarget.getValue(spnTarget.getSelectedItemPosition())));
		mask.setAction(MaskActionType.valueOf(adpAction.getValue(spnAction.getSelectedItemPosition())));
		mask.setBank(BankType.valueOf(adpBank.getValue(spnBank.getSelectedItemPosition())));
		mask.setPointer(Integer.parseInt(edtOffset.getText().toString()));
		mask.setMask(edtMask.getText().toString());
		mask.setLength(Integer.parseInt(edtLength.getText().toString()));
		
		Log.i(TAG, String.format(Locale.US, "INFO. getSelectionMask() - [%s]", mask));
		return mask;
	}
	
	public void show(int position, SelectionMask6c mask) {
		
		mPosition = position;
		mIsUsed = mask.isUsed();
		spnTarget.setSelection(adpTarget.indexOf(mask.getTarget().getValue()));
		spnAction.setSelection(adpAction.indexOf(mask.getAction().getValue()));
		spnBank.setSelection(adpBank.indexOf(mask.getBank().getValue()));
		edtOffset.setText(String.format(Locale.US, "%d", mask.getPointer()));
		edtMask.setText(mask.getMask());
		edtLength.setText(String.format(Locale.US, "%d", mask.getLength()));
		
		mDialog.show();
		
		Log.i(TAG, String.format(Locale.US, "INFO. show(%d, %s)", position, mask));
	}
	
	private TextWatcher maskWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (edtMask.isFocusable()) {
				int length = s.length() * NIBBLE_UNIT;
				edtLength.setText(String.format(Locale.US, "%d", length));
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
		
	};
}
