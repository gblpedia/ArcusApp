package com.atid.app.rfid.dialog;

import au.com.adilamtech.app.arcus.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PasswordDialog implements OnCancelListener, OnClickListener {

	private static final String TAG = PasswordDialog.class.getSimpleName();
	
	private EditText edtPassword;
	
	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;
	
	private IDialogResultListener mListener;
	
	public PasswordDialog(Context context, int title) {
		mListener = null;
		LinearLayout root = (LinearLayout)LinearLayout.inflate(context, R.layout.dialog_password, null);
		edtPassword = (EditText)root.findViewById(R.id.password);
		edtPassword.setText("00000000");
		
		mBuilder = new AlertDialog.Builder(context);
		mBuilder.setTitle(title);
		mBuilder.setView(root);
		mBuilder.setPositiveButton(R.string.action_ok, this);
		mBuilder.setNegativeButton(R.string.action_cancel, this);
		
		mDialog = mBuilder.create();
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			if (this.mListener != null) 
				this.mListener.onOkClick(0, dialog);
			break;
		case Dialog.BUTTON_NEGATIVE:
			if (this.mListener != null)
				this.mListener.onCancelClick(0, dialog);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (this.mListener != null)
			this.mListener.onCancelClick(0, dialog);
	}
	
	public void show() {
		mDialog.show();
	}
	
	public Dialog getDialog() {
		return this.mDialog;
	}
	
	public void setResultListener(IDialogResultListener listener) {
		this.mListener = listener;
	}
	
	public void setPassword(String password) {
		this.edtPassword.setText(password);
	}
	
	public String getPassword() {
		return this.edtPassword.getText().toString();
	}

	public interface IDialogResultListener {
		void onOkClick(int what, DialogInterface dialog);
		void onCancelClick(int what, DialogInterface dialog);
	}
}
