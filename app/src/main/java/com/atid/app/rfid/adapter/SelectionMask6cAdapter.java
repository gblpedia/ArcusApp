package com.atid.app.rfid.adapter;

import java.util.ArrayList;
import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.util.StringUtil;
import com.atid.lib.dev.rfid.param.SelectionMask6c;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SelectionMask6cAdapter extends BaseAdapter {

	private static final int MAX_ITEM_COUNT = 8;

	private LayoutInflater mInflater;
	private ArrayList<SelectionMask6c> mList;

	public SelectionMask6cAdapter(Context context) {
		super();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mList = new ArrayList<SelectionMask6c>();

		for (int i = 0; i < MAX_ITEM_COUNT; i++) {
			mList.add(new SelectionMask6c());
		}
	}

	public void setItem(int position, SelectionMask6c item) {
		mList.set(position, item);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public SelectionMask6c getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SelectionMaskViewHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_selection_mask_6c,
					parent, false);
			holder = new SelectionMaskViewHolder(convertView);
		} else {
			holder = (SelectionMaskViewHolder) convertView.getTag();
		}
		holder.setItem(position, mList.get(position));
		return convertView;
	}

	// ------------------------------------------------------------------------
	// Class SelectionMaskViewHolder
	// ------------------------------------------------------------------------
	private class SelectionMaskViewHolder implements OnCheckedChangeListener {

		private CheckBox chkUsed;
		private TextView txtTarget;
		private TextView txtAction;
		private TextView txtBank;
		private TextView txtOffset;
		private TextView txtLength;
		private TextView txtMask;

		public SelectionMaskViewHolder(View parent) {

			chkUsed = (CheckBox) parent.findViewById(R.id.used);
			chkUsed.setOnCheckedChangeListener(this);
			txtTarget = (TextView) parent.findViewById(R.id.target);
			txtAction = (TextView) parent.findViewById(R.id.action);
			txtBank = (TextView) parent.findViewById(R.id.bank);
			txtOffset = (TextView) parent.findViewById(R.id.offset);
			txtLength = (TextView) parent.findViewById(R.id.length);
			txtMask = (TextView) parent.findViewById(R.id.mask);
			parent.setTag(this);
		}

		public void setItem(int position, SelectionMask6c item) {

			chkUsed.setTag(new Integer(position));
			chkUsed.setChecked(item.isUsed());
			txtTarget.setText(item.getTarget().toString());
			txtAction.setText(StringUtil.toString(item.getAction()));
			txtBank.setText(item.getBank().toString());
			txtOffset.setText(String.format(Locale.US, "%d bit", item.getPointer()));
			txtLength.setText(String.format(Locale.US, "%d bit", item.getLength()));
			txtMask.setText(item.getMask());
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			if (buttonView instanceof CheckBox) {
				CheckBox check = (CheckBox) buttonView;
				int position = ((Integer) check.getTag()).intValue();
				SelectionMask6c item = mList.get(position);
				if (item != null) {
					item.setUsed(isChecked);
				}
			}
		}
	}
}
