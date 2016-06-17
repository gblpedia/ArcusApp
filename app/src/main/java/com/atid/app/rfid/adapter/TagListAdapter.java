package com.atid.app.rfid.adapter;

import java.util.ArrayList;
import java.util.BitSet;
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
	private boolean mIsShowAscii;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TagListAdapter(Context context) {
		super();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mList = new ArrayList<TagListItem>();
		mMap = new HashMap<String, TagListItem>();

		mIsDisplayPc = false;
		mIsVisibleRssi = false;
		mIsShowAscii = true;
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

    public boolean isShowAscii() {
        return mIsShowAscii;
    }

	public void setDisplayPc(boolean enabled) {
		mIsDisplayPc = enabled;
		notifyDataSetChanged();
	}

	public void setShowAscii(boolean enabled) {
		mIsShowAscii = enabled;
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

	public boolean isExistedItem(String tag) {

		TagListItem item = null;
		boolean isExisted = false;
		Log.i(TAG, String.format(Locale.US, "INFO. Existed Item [%s]", tag));

		isExisted = ((item = mMap.get(tag)) == null);

		return isExisted;
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

		holder.setItem(mList.get(position), mIsDisplayPc, mIsVisibleRssi, mIsShowAscii);


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


        public String itemID() {
            int length = lengthOfItemID(mTag.substring(0, PC_LEN));
            String itemIdInTag = mTag.substring(PC_LEN, PC_LEN + length);

            return itemIdInTag;
        }

        private byte[] hexStringToByteArray(String s) {
            byte[] b = new byte[s.length() / 2];
            for (int i = 0; i < b.length; i++) {
                int index = i * 2;
                int v = Integer.parseInt(s.substring(index, index + 2), 16);
                b[i] = (byte) v;
            }
            return b;
        }

        private BitSet longToBitSet(long value) {
            BitSet bits = new BitSet();
            int index = 0;
            while (value != 0L) {
                if (value % 2L != 0) {
                    bits.set(index);
                }
                ++index;
                value = value >>> 1;
            }
            return bits;
        }

        private long bitsetToLong(BitSet bits) {
            long value = 0L;
            for (int i = 0; i < bits.length(); ++i) {
                value += bits.get(i) ? (1L << i) : 0L;
            }
            return value;
        }

        private BitSet fromByteArray(byte[] bytes) {
            BitSet bits = new BitSet();
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                    bits.set(i);
                }
            }
            return bits;
        }


        private int lengthOfItemID(String pc) {
            int length = 0;
            byte[] pcBytes =  hexStringToByteArray(pc.substring(0, PC_LEN));
            BitSet pcBitSet = fromByteArray(pcBytes);
            BitSet lenBitSet = pcBitSet.get(11, 16);

            length = (int) bitsetToLong(lenBitSet);

            return length * 4;
        }

        public String tagInAscii(){

            StringBuilder sb = new StringBuilder();
            StringBuilder temp = new StringBuilder();
            String item_id = itemID();

            //49204c6f7665204a617661 split into two characters 49, 20, 4c...
            for( int i=0; i < item_id.length()-1; i+=2 ){

                //grab the hex in pairs
                String output = item_id.substring(i, (i + 2));
                //convert hex to decimal
                int decimal = Integer.parseInt(output, 16);
                //convert the decimal to character
                sb.append((char)decimal);

                temp.append(decimal);
            }
            System.out.println("Decimal : " + temp.toString());

            return sb.toString();
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
				boolean visibleRssi, boolean showAscii) {

            if (showAscii) {
                txtTag.setText(item.tagInAscii());
            } else {
                txtTag.setText(item.getTag().substring(PC_LEN));
            }

			/*if (displayPc) {
				txtTag.setText(item.getTag());
			} else {
				txtTag.setText(item.getTag().substring(PC_LEN));
			}*/

			txtRssi.setVisibility(visibleRssi ? View.VISIBLE : View.GONE);
			if (visibleRssi) {
				txtRssi.setText(String.format(Locale.US, "%.1f dB", item.getRssi()));
			}
			txtCount.setText(String.format(Locale.US, "%d", item.getCount()));
		}
	}
}
