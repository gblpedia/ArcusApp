package com.atid.app.rfid.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.adapter.SpinnerValueAdapter;
import com.atid.app.rfid.dialog.WaitDialog;
import com.atid.app.rfid.view.base.ReaderActivity;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.RangeValue;
import com.atid.lib.dev.rfid.type.GlobalBandType;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class OptionActivity extends ReaderActivity implements OnClickListener {

	private static final int MAX_POWER_LEVEL = 300;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private AutoCompleteTextView edtOperationTime;
	private TextView txtGlobalBand;
	private AutoCompleteTextView edtInventoryTime;
	private AutoCompleteTextView edtIdleTime;
	private Spinner spnPower;
	private Button btnFreqChannel;
	private Button btnSave;
	private Button btnDefaults;

	private ArrayAdapter<String> adpOperationTime;
	private ArrayAdapter<String> adpInventoryTime;
	private ArrayAdapter<String> adpIdleTime;
	private SpinnerValueAdapter adpPower;

	private RangeValue mPowerRange;
	private GlobalBandType mGlobalBand;
	private int mOperationTime;
	private int mInventoryTime;
	private int mIdleTime;
	private int mPowerLevel;
	private String[] mFreqChanNames;
	private boolean[] mFreqChanUses;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public OptionActivity() {
		super();

		TAG = OptionActivity.class.getSimpleName();
		mView = R.layout.activity_option;

		mPowerRange = null;
		mGlobalBand = GlobalBandType.Korea;
		mPowerLevel = MAX_POWER_LEVEL;
		mOperationTime = 0;
		mInventoryTime = 0;
		mIdleTime = 0;
		mFreqChanNames = null;
		mFreqChanUses = null;
	}

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		enableWidgets(false);

		switch (v.getId()) {
		case R.id.freq_channel:
			showFreqChannelDialog();
			enableWidgets(true);
			break;
		case R.id.save:
			saveOption();
			break;
		case R.id.option_default:
			defaultOption();
			break;
		}
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	@Override
	protected void initWidgets() {

		// Initialize Operation Time AutoCompleteTextView
		edtOperationTime = (AutoCompleteTextView) findViewById(R.id.operation_time);
		adpOperationTime = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.operation_time));
		edtOperationTime.setAdapter(adpOperationTime);

		// Initialize Global Band TextView
		txtGlobalBand = (TextView) findViewById(R.id.global_band);

		// Initialize Inventory Time AutoCompleteTextView
		edtInventoryTime = (AutoCompleteTextView) findViewById(R.id.inventory_time);
		adpInventoryTime = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.reader_time));
		edtInventoryTime.setAdapter(adpInventoryTime);

		// Initialize Idle Time AutoCompleteTextView
		edtIdleTime = (AutoCompleteTextView) findViewById(R.id.idle_time);
		adpIdleTime = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				getResources().getStringArray(R.array.reader_time));
		edtIdleTime.setAdapter(adpIdleTime);

		// Initialize Power Gain
		spnPower = (Spinner) findViewById(R.id.power_gain);
		adpPower = new SpinnerValueAdapter(this, android.R.layout.simple_list_item_1);
		spnPower.setAdapter(adpPower);

		// Initialize Freq Channel
		btnFreqChannel = (Button) findViewById(R.id.freq_channel);
		btnFreqChannel.setOnClickListener(this);

		// Initialize Save Button
		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		// Initialize Default Button
		btnDefaults = (Button) findViewById(R.id.option_default);
		btnDefaults.setOnClickListener(this);
	}

	// Enable/Disable Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		edtOperationTime.setEnabled(enabled);
		edtInventoryTime.setEnabled(enabled);
		edtIdleTime.setEnabled(enabled);
		spnPower.setEnabled(enabled);
		btnFreqChannel.setEnabled(enabled);
		btnSave.setEnabled(enabled);
		btnDefaults.setEnabled(enabled);
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

		// Get Global Band
		try {
			mGlobalBand = mReader.getGlobalBand();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get global band [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Global Band : %s]", mGlobalBand));

		// Get Operation Time
		try {
			mOperationTime = mReader.getOperationTime();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get operation time [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Operation Time : %d]", mOperationTime));

		// Get Inventory Time
		try {
			mInventoryTime = mReader.getInventoryTime();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get inventory time [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Inventory Time : %d]", mInventoryTime));

		// Get Idle Time
		try {
			mIdleTime = mReader.getIdleTime();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get idle time [%s]", e.getCode()), e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Idle Time : %d]", mIdleTime));

		// Get Freq Table
		int count = 0;
		int freq = 0;

		try {
			count = mReader.getFreqChannelCount();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get frequency channel count [%s]",
					e.getCode()), e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Frequency Channel Count : %d]", count));

		mFreqChanNames = new String[count];
		mFreqChanUses = new boolean[count];

		for (int i = 0; i < count; i++) {
			try {
				freq = mReader.getChannelFreq(i);
			} catch (ATRfidReaderException e) {
				Log.e(TAG, String.format(Locale.US,
						"ERROR. initReader() - Failed to get frequency channel name [%d] [%s]", i, e.getCode()), e);
			}
			Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Frequency Channel name [%d] : %d]", i, count));
			mFreqChanNames[i] = getFreqString(freq);
			try {
				mFreqChanUses[i] = mReader.isUseFreqChannel(i);
			} catch (ATRfidReaderException e) {
				Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get channel frequency [%d] [%s]",
						i, e.getCode()), e);
			}
			Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Channel Frequency [%d] : %d]", i, count));
		}
	}

	private String getFreqString(int freq) {
		if (freq > 1000000000)
			return String.format(Locale.US, "%.1fGHz", (double) freq / 1000000000.0);
		else if (freq > 1000000)
			return String.format(Locale.US, "%.1fMHz", (double) freq / 1000000.0);
		else if (freq > 1000)
			return String.format(Locale.US, "%.1fKHz", (double) freq / 1000.0);
		return String.format(Locale.US, "%d", freq);
	}

	// Activated Reader
	@Override
	protected void activateReader() {

		// Fill Power Spinner
		for (int i = mPowerRange.getMax(); i >= mPowerRange.getMin(); i -= 10) {
			adpPower.addItem(i, String.format(Locale.US, "%.1f dBm", ((double) i / 10.0)));
		}
		adpPower.notifyDataSetChanged();
		// Set Power Level
		setPowerLevel(mPowerLevel);

		// Set Global Band
		txtGlobalBand.setText(mGlobalBand.toString());

		// Set Operation Time
		setOperationTime(mOperationTime);

		// Set Invenotry Time
		setInventoryTime(mInventoryTime);

		// Set Idle Time
		setIdleTime(mIdleTime);

		enableWidgets(true);
	}

	// ------------------------------------------------------------------------
	// Override Widgets Access Methods
	// ------------------------------------------------------------------------

	private int getPowerLevel() {
		int position = spnPower.getSelectedItemPosition();
		return adpPower.getValue(position);
	}

	private void setPowerLevel(int power) {
		int position = adpPower.indexOf(power);
		spnPower.setSelection(position);
	}

	private int getOperationTime() {
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

	private void setOperationTime(int time) {
		edtOperationTime.setText(String.format(Locale.US, "%d", time));
	}

	private int getInventoryTime() {
		String value = edtInventoryTime.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(value);
		} catch (Exception e) {
			Log.e(TAG,
					String.format(Locale.US, "ERROR. getInvenotryTime() - Failed to parse inventory time [%s]", value),
					e);
			time = 0;
		}
		return time;
	}

	private void setInventoryTime(int time) {
		edtInventoryTime.setText(String.format(Locale.US, "%d", time));
	}

	private int getIdleTime() {
		String value = edtIdleTime.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(value);
		} catch (Exception e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. getIdleTime() - Failed to parse idle time [%s]", value), e);
			time = 0;
		}
		return time;
	}

	private void setIdleTime(int time) {
		edtIdleTime.setText(String.format(Locale.US, "%d", time));
	}

	// ------------------------------------------------------------------------
	// Background Work Methods
	// ------------------------------------------------------------------------

	private void saveOption() {
		WaitDialog.show(this, "Save Properties...\r\nPlease Wait...");

		mOperationTime = getOperationTime();
		mInventoryTime = getInventoryTime();
		mIdleTime = getIdleTime();
		mPowerLevel = getPowerLevel();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// Set Operation Time
				try {
					mReader.setOperationTime(mOperationTime);
				} catch (ATRfidReaderException e) {
					Log.e(TAG, String.format(Locale.US, "ERROR. saveOption() - Failed to set operation Time [%s]",
							e.getCode()), e);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();
							enableWidgets(true);
						}
					});
					return;
				}
				Log.i(TAG, String.format(Locale.US, "INFO. saveOption() - [Operation Time : %d]", mOperationTime));

				// Set Inventory Time
				try {
					mReader.setInventoryTime(mInventoryTime);
				} catch (ATRfidReaderException e) {
					Log.e(TAG, String.format(Locale.US, "ERROR. saveOption() - Failed to set inventory Time [%s]",
							e.getCode()), e);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();
							enableWidgets(true);
						}
					});
					return;
				}
				Log.i(TAG, String.format(Locale.US, "INFO. saveOption() - [Inventory Time : %d]", mInventoryTime));

				// Set Idle Time
				try {
					mReader.setIdleTime(mIdleTime);
				} catch (ATRfidReaderException e) {
					Log.e(TAG,
							String.format(Locale.US, "ERROR. saveOption() - Failed to set idle Time [%s]", e.getCode()),
							e);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();
							enableWidgets(true);
						}
					});
					return;
				}
				Log.i(TAG, String.format(Locale.US, "INFO. saveOption() - [Idle Time : %d]", mIdleTime));

				// Set Power Level
				try {
					mReader.setPower(mPowerLevel);
				} catch (ATRfidReaderException e) {
					Log.e(TAG, String.format(Locale.US, "ERROR. saveOption() - Failed to set power level [%s]",
							e.getCode()), e);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();
							enableWidgets(true);
						}
					});
					return;
				}
				Log.i(TAG, String.format(Locale.US, "INFO. saveOption() - [Power Level : %d]", mPowerLevel));

				// Frequency Channel
				for (int i = 0; i < mFreqChanUses.length; i++) {
					try {
						mReader.setUseFreqChannel(i, mFreqChanUses[i]);
					} catch (ATRfidReaderException e) {
						Log.e(TAG,
								String.format(Locale.US,
										"ERROR. saveOption() - Failed to set use frequency channel [%d] [%s]", i,
										e.getCode()),
								e);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								WaitDialog.hide();
								enableWidgets(true);
							}
						});
						return;
					}
					Log.i(TAG, String.format(Locale.US, "INFO. saveOption() - [Freq Channel [%d] : %s]", i,
							mFreqChanUses[i]));
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						WaitDialog.hide();
						enableWidgets(true);
						finish();
					}
				});
			}
		}).start();
	}

	private void defaultOption() {
		WaitDialog.show(this, "Setting Default Properties...\r\nPlease Wait...");
		new Thread(new Runnable() {

			@Override
			public void run() {

				mReader.defaultProperties();
				initReader();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						activateReader();
						WaitDialog.hide();
					}
				});
			}
		}).start();
	}

	private void showFreqChannelDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.freq_chan_title);
		builder.setMultiChoiceItems(mFreqChanNames, mFreqChanUses, onFreqChecked);
		builder.setPositiveButton(R.string.action_ok, null);
		builder.setNegativeButton(R.string.action_cancel, null);
		builder.setCancelable(true);
		builder.create().show();
	}

	private OnMultiChoiceClickListener onFreqChecked = new OnMultiChoiceClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			mFreqChanUses[which] = isChecked;
		}

	};
}