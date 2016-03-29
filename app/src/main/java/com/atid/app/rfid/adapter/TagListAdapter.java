package com.atid.app.rfid.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import au.com.adilamtech.app.arcus.R;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagListAdapter extends BaseAdapter {

	private static final String TAG = TagListAdapter.class.getSimpleName();

	public static final int PC_LEN = 4;
	private static final int UPDATE_TIME = 500;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private LayoutInflater mInflater;

	private ArrayList<TagListItem> mList;
	private HashMap<String, TagListItem> mMap;

	private boolean mIsDisplayPc;
	private boolean mIsVisibleRssi;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TagListAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mList = new ArrayList<TagListItem>();
		mMap = new HashMap<String, TagListItem>();

		mIsDisplayPc = true;
		mIsVisibleRssi = false;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	public void clear() {
		mList.clear();
		mMap.clear();
		notifyDataSetChanged();
	}

	public boolean isDisplayPc() {
		return mIsDisplayPc;
	}

	public void setDisplayPc(boolean enabled) {
		mIsDisplayPc = enabled;
		notifyDataSetChanged();
	}

	public boolean isVisibleRssi() {
		return mIsVisibleRssi;
	}

	public void setVisibleRssi(boolean visibled) {
		mIsVisibleRssi = visibled;
		notifyDataSetChanged();
	}

	public void addItem(String tag, double rssi) {
		TagListItem item = null;

		Log.i(TAG, String.format(Locale.US, "INFO. addItem([%s], %.1f", tag, rssi));

		if ((item = mMap.get(tag)) == null) {
			item = new TagListItem(tag, rssi);
			mList.add(item);
			mMap.put(tag, item);
		} /*else {
			item.updateTag(rssi);  Gabriel
		}*/
	}


    public void updateUserSpace(String tag, String data) {
        TagListItem item = null;
        item = mMap.get(tag);

        if( item != null ) {
            item.setUserSpace(data);
        }
    }


    public TagListItem getItemByTag(String tag) {

        return mMap.get(tag);
    }


	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position).getTag();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TagListViewHolder holder;

		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_tag_list, parent,
					false);
			holder = new TagListViewHolder(convertView);
		} else {
			holder = (TagListViewHolder) convertView.getTag();
		}
		holder.setItem(mList.get(position), mIsDisplayPc, mIsVisibleRssi);

		return convertView;
	}

	// ------------------------------------------------------------------------
	// Internal Class TagListItem
	// ------------------------------------------------------------------------

	private class TagListItem {

		private String mTag;
		private double mRssi;
		private int mCount;
		private String mUserSpace;

		public TagListItem(String tag, double rssi) {
			mTag = tag;
			mRssi = rssi;
			mCount = 1;
		}

		public void setUserSpace(String data) { mUserSpace = data; }

		public String getTag() {
			return mTag;
		}

		public double getRssi() {
			return mRssi;
		}

		public String getUserSpace() {return mUserSpace; }

		public int getCount() {
			return mCount;
		}

		public void updateTag(double rssi) {
			mRssi = rssi;
			mCount++;
		}
	}

	// ------------------------------------------------------------------------
	// Internal Class TagListViewHolder
	// ------------------------------------------------------------------------

	private class TagListViewHolder {

		private TextView txtTag;
		private TextView txtRssi;
		private TextView txtCount;

		public TagListViewHolder(View parent) {
			txtTag = (TextView) parent.findViewById(R.id.tag_value);
			txtRssi = (TextView) parent.findViewById(R.id.rssi_value);
			txtCount = (TextView) parent.findViewById(R.id.tag_count);
			parent.setTag(this);
		}

		public void setItem(TagListItem item, boolean displayPc,
				boolean visibleRssi) {
			if (displayPc) {
				txtTag.setText(item.getTag());
			} else {
				txtTag.setText(item.getTag().substring(PC_LEN));
			}
			txtRssi.setVisibility(visibleRssi ? View.VISIBLE : View.GONE);
			if (visibleRssi) {
				txtRssi.setText(String.format(Locale.US, "%.1f dB", item.getRssi()));
			}
			txtCount.setText(String.format(Locale.US, "%d", item.getCount()));
		}
	}
}
