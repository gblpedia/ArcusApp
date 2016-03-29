package com.atid.app.rfid.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SpinnerValueAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<StringValueItem> list;
	private int res;
	private int dropDownRes;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public SpinnerValueAdapter(Context context, int res) {
		super();
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<StringValueItem>();
		this.res = res;
		this.dropDownRes = 0;
	}

	public SpinnerValueAdapter(Context context, int res, int dropDownRes) {
		super();
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = new ArrayList<StringValueItem>();
		this.res = res;
		this.dropDownRes = dropDownRes;
	}

	public int indexOf(int value) {
		for (int i = 0; i < this.list.size(); i++) {
			if (this.list.get(i).getValue() == value) {
				return i;
			}
		}
		return -1;
	}

	public void addItem(int value, String name) {
		this.list.add(new StringValueItem(value, name));
	}

	public void addItem(String[] names) {
		for (int i = 0; i < names.length; i++) {
			this.list.add(new StringValueItem(i, names[i]));
		}
	}

	public int getValue(int position) {
		return this.list.get(position).getValue();
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Integer getItem(int position) {
		return this.list.get(position).getValue();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StringValueViewHolder holder;

		if (null == convertView) {
			convertView = inflater.inflate(this.res, parent, false);
			holder = new StringValueViewHolder(convertView);
		} else {
			holder = (StringValueViewHolder) convertView.getTag();
		}
		holder.setItem(this.list.get(position));

		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		StringValueViewHolder holder;

		if (null == convertView) {
			convertView = inflater.inflate(this.dropDownRes == 0 ? this.res
					: this.dropDownRes, parent, false);
			holder = new StringValueViewHolder(convertView);
		} else {
			holder = (StringValueViewHolder) convertView.getTag();
		}
		holder.setItem(this.list.get(position));

		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal Class StringValueItem
	// ------------------------------------------------------------------------

	private class StringValueItem {

		private int value;
		private String name;

		public StringValueItem(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return this.value;
		}

		public String toString() {
			return this.name;
		}
	}

	// ------------------------------------------------------------------------
	// Internal Class StringValueViewHolder
	// ------------------------------------------------------------------------

	private class StringValueViewHolder {

		private TextView text;

		public StringValueViewHolder(View parent) {
			this.text = (TextView) parent.findViewById(android.R.id.text1);
			parent.setTag(this);
		}

		public void setItem(StringValueItem item) {
			this.text.setText(item.toString());
		}
	}
}
