package com.atid.app.rfid.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.channels.UnresolvedAddressException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

import org.apache.http.impl.client.DefaultUserTokenHandler;
import org.jboss.netty.channel.ChannelException;
import org.json.*;
import org.projectodd.stilts.stomp.DefaultHeaders;
import org.projectodd.stilts.stomp.DefaultStompMessage;
import org.projectodd.stilts.stomp.Headers;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.StompMessages;
import org.projectodd.stilts.stomp.Subscription;
import org.projectodd.stilts.stomp.client.ClientListener;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.ClientTransaction;
import org.projectodd.stilts.stomp.client.MessageHandler;
import org.projectodd.stilts.stomp.client.StompClient;
import org.projectodd.stilts.stomp.protocol.StompFrame;


import com.atid.app.rfid.GlobalInfo;

import au.com.adilamtech.app.arcus.ArcusConnection;
import au.com.adilamtech.app.arcus.ArcusConnectionEventListener;
import au.com.adilamtech.app.arcus.ArcusConnectionEventListener;
import au.com.adilamtech.app.arcus.R;
import com.atid.app.rfid.adapter.TagListAdapter;
import com.atid.app.rfid.view.base.AccessActivity;
import com.atid.app.rfid.view.base.ActionActivity;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.InventorySession;
import com.atid.lib.dev.rfid.type.InventoryTarget;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;
import com.atid.lib.util.SysUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import javax.net.ssl.SSLException;

public class InventoryActivity extends ActionActivity implements OnCheckedChangeListener, ArcusConnectionEventListener {

	private static final int READ_MEMORY_ACTIVITY = 1;
	private static final int WRITE_MEMORY_ACTIVITY = 2;
	private static final int LOCK_MEMORY_ACTIVITY = 3;
	private static final int UPDATE_TIME = 500;
	private static final int OFFSET_USERSPACE = 0;
	private static final int LENGTH_USERSPACE = 128;
	private static final int ITEM_ID_START_INDEX = 4;
	//private static final int ITEM_ID_LENGTH = 28;

    /*-------------------------------------------------------------------------
    * Arucs Settings
    * ------------------------------------------------------------------------*/
    private String arcus_account;
    private String arcus_station_id;
    private String arcus_location;
    private String timezone_offset;



	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private ListView lstTags;
	private CheckBox chkDisplayPc;
	private CheckBox chkContinuousMode;
	private CheckBox chkReportRssi;

	private TextView txtCount;
	private Button btnAction;
	private Button btnSave;
	private Button btnUpload;

	private TagListAdapter adpTags;

	private MenuItem mnuReadMemory;
	private MenuItem mnuWriteMemory;
	private MenuItem mnuLockMemory;

	private boolean mIsReportRssi;

	private Thread mThread;
	private boolean mIsAliveThread;
    private ProgressDialog uploadingProgress;



	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public InventoryActivity() {
		super();

		TAG = InventoryActivity.class.getSimpleName();
		mView = R.layout.activity_inventory;

		mIsReportRssi = false;

		mThread = null;
		mIsAliveThread = false;
	}

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------


	@Override
	public void onClick(View v) {
		super.onClick(v);

		switch (v.getId()) {
			case R.id.save:
				saveInventory();
				break;
			case R.id.upload:
				uploadInventory(v);
				break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.continue_mode:
			GlobalInfo.setContinuousMode(chkContinuousMode.isChecked());
			break;
		case R.id.display_pc:
			GlobalInfo.setDisplayPc(chkDisplayPc.isChecked());
			adpTags.setDisplayPc(GlobalInfo.isDisplayPc());
			break;
		case R.id.report_rssi:
			adpTags.setVisibleRssi(chkReportRssi.isChecked());
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.tag_list && mReader.getAction() == ActionState.Stop) {
			getMenuInflater().inflate(R.menu.context_menu, menu);

			mnuReadMemory = menu.findItem(R.id.read_memory);
			mnuWriteMemory = menu.findItem(R.id.write_memory);
			mnuLockMemory = menu.findItem(R.id.lock_memory);

			switch (getTagType()) {
			case Tag6C:
				mnuReadMemory.setVisible(true);
				mnuWriteMemory.setVisible(true);
				mnuLockMemory.setVisible(true);
				break;
			case Tag6B:
				mnuReadMemory.setVisible(true);
				mnuWriteMemory.setVisible(false);
				mnuLockMemory.setVisible(false);
				break;
			default:
				mnuReadMemory.setVisible(false);
				mnuWriteMemory.setVisible(false);
				mnuLockMemory.setVisible(false);
				break;
			}
			enableWidgets(false);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = menuInfo.position;

		if (position < 0)
			return false;

		enableWidgets(false);
		mnuReadMemory.setEnabled(false);
		mnuWriteMemory.setEnabled(false);
		mnuLockMemory.setEnabled(false);

		String tag = adpTags.getItem(position);

		switch (item.getItemId()) {
		case R.id.read_memory:
			intent = new Intent(this, ReadMemoryActivity.class);
			intent.putExtra(AccessActivity.KEY_EPC, tag);
			startActivityForResult(intent, READ_MEMORY_ACTIVITY);
			break;
		case R.id.write_memory:
			intent = new Intent(this, WriteMemoryActivity.class);
			intent.putExtra(AccessActivity.KEY_EPC, tag);
			startActivityForResult(intent, WRITE_MEMORY_ACTIVITY);
			break;
		case R.id.lock_memory:
			intent = new Intent(this, LockMemoryActivity.class);
			intent.putExtra(AccessActivity.KEY_EPC, tag);
			startActivityForResult(intent, LOCK_MEMORY_ACTIVITY);
			break;
		}
		return true;
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		enableWidgets(true);
	}

	// ------------------------------------------------------------------------
	// Reader Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {

		if (action == ActionState.Stop) {
			stopUpdateList();
		}

		enableWidgets(true);

		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderActionchanged(%s)", action));
	}

	@Override
	public void onReaderReadTag(ATRfidReader reader, final String tag, final float rssi) {

		runOnUiThread(new Runnable() {

            @Override
            public void run() {
                synchronized (adpTags) {
                    adpTags.addItem(tag, rssi);
                }
                txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
                playSuccess();
            }

        });

		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderReadTag([%s], %.2f)", tag, rssi));
	}


	@Override
	public void onCompletion() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), R.string.toast_upload_successfully, Toast.LENGTH_SHORT).show();
                btnUpload.setEnabled(false);
                //uploadingProgress.setProgress(100);
                uploadingProgress.dismiss();
            }
        });

        String fileName = getResources().getString(R.string.file_name);
        String dirName = getResources().getString(R.string.folder_name);
        File arcusDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName );
        File inventoryFd = new File(arcusDir.getAbsolutePath() + File.separator + fileName);
        inventoryFd.delete();

	}


	@Override
	public void onException(Exception e) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), R.string.toast_network_failed, Toast.LENGTH_SHORT).show();
                uploadingProgress.cancel();
            }
        });

        Log.e("ArcusConnection", "ERROR. Exception", e);
	}
	
	// ------------------------------------------------------------------------
	// Reader Control Methods
	// ------------------------------------------------------------------------

	// Start Action
	protected void startAction() {

		ResultCode res;
		TagType tagType = getTagType();

		enableWidgets(false);
		startUpdateList();

		if (chkContinuousMode.isChecked()) {
			// Multi Reading
			switch (tagType) {
			case Tag6C:
				if ((res = mReader.inventory6cTag()) != ResultCode.NoError) {
					Log.e(TAG, String.format(Locale.US, "ERROR. startAction() - Failed to start inventory 6C tag [%s]",
							res));
					stopUpdateList();
					enableWidgets(true);
					return;
				}
				break;
			case Tag6B:
				if ((res = mReader.inventory6bTag()) != ResultCode.NoError) {
					Log.e(TAG, String.format(Locale.US, "ERROR. startAction() - Failed to start inventory 6B tag [%s]",
							res));
					stopUpdateList();
					enableWidgets(true);
					return;
				}
				break;
			}
		} else {
			// Single Reading
			switch (tagType) {
			case Tag6C:
				if ((res = mReader.readEpc6cTag()) != ResultCode.NoError) {
					Log.e(TAG,
							String.format(Locale.US, "ERROR. startAction() - Failed to start read 6C tag [%s]", res));
					stopUpdateList();
					enableWidgets(true);
					return;
				}
				break;
			case Tag6B:
				if ((res = mReader.readEpc6bTag()) != ResultCode.NoError) {
					Log.e(TAG,
							String.format(Locale.US, "ERROR. startAction() - Failed to start read 6B tag [%s]", res));
					stopUpdateList();
					enableWidgets(true);
					return;
				}
				break;
			}
        }
        Log.i(TAG, "INFO. startAction()");
	}

	// ------------------------------------------------------------------------
	// Override Widgets Control Methods
	// ------------------------------------------------------------------------

	@Override
	// Clear Widgets
	protected void clear() {
		adpTags.clear();
		txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
		btnSave.setEnabled(false);

		Log.i(TAG, "INFO. clear()");
	}

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Initialize Tag List View
		lstTags = (ListView) findViewById(R.id.tag_list);
		adpTags = new TagListAdapter(this);
		lstTags.setAdapter(adpTags);
		lstTags.setOnItemSelectedListener(this);
		registerForContextMenu(lstTags);

		// Display PC Check Box
		chkDisplayPc = (CheckBox) findViewById(R.id.display_pc);
		chkDisplayPc.setOnCheckedChangeListener(this);

		// Continuous Mode Check Box
		chkContinuousMode = (CheckBox) findViewById(R.id.continue_mode);
		chkContinuousMode.setOnCheckedChangeListener(this);

		// Display RSSI Check Box
		chkReportRssi = (CheckBox) findViewById(R.id.report_rssi);
		chkReportRssi.setOnCheckedChangeListener(this);

		// Tag Count
		txtCount = (TextView) findViewById(R.id.tag_count);

		// Action Button
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnUpload = (Button) findViewById(R.id.upload);
		btnUpload.setOnClickListener(this);

        arcus_station_id = getResources().getString(R.string.arucs_station_id);
        arcus_account = getResources().getString(R.string.arcus_account);
        arcus_location = getResources().getString(R.string.arcus_location);
        timezone_offset = getResources().getString(R.string.timezone_offset);

		Log.i(TAG, "INFO. initWidgets()");
	}

	// Eanble Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);

		if (mReader.getAction() == ActionState.Stop) {
			/*chkDisplayPc.setEnabled(enabled);
			chkContinuousMode.setEnabled(enabled);
			chkReportRssi.setEnabled(enabled);*/
			btnAction.setText(R.string.action_inventory);

			if(!adpTags.isEmpty()) {
				btnSave.setEnabled(true);
			}

		} else {
			/*chkDisplayPc.setEnabled(false);
			chkContinuousMode.setEnabled(false);
			chkReportRssi.setEnabled(false);*/
			btnAction.setText(R.string.action_stop);
			btnSave.setEnabled(false);
		}
		btnAction.setEnabled(enabled);

		if(isFileExist()) {
			btnUpload.setEnabled(true);
		}

	}

	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();

		// Get Report RSSI
		try {
			mIsReportRssi = mReader.getReportRssi();
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get report RSSI [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Report RSSI : %s]", mIsReportRssi));

		try {
			mReader.setInventorySession(InventorySession.S2);
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to set InventorySession.S2 [%s]", e.getCode()),
					e);
		}

		try {
			mReader.setInventoryTarget(InventoryTarget.B);
		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to set InventoryTarget.B [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, "INFO initReader() with Session2 and TargetB");
	}

	// Activated Reader
	@Override
	protected void activateReader() {
		super.activateReader();


		chkDisplayPc.setChecked(GlobalInfo.isDisplayPc());
		chkContinuousMode.setChecked(GlobalInfo.isContinuousMode());
		chkReportRssi.setChecked(mIsReportRssi);
		adpTags.setDisplayPc(GlobalInfo.isContinuousMode());
		adpTags.setVisibleRssi(chkReportRssi.isChecked());

		enableWidgets(true);

		Log.i(TAG, "INFO. activateReader()");
	}


	private void saveInventory() {
		int count = adpTags.getCount();
		//SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy_HHmmss");
		//String fileName = dt.format(new Date()).concat(".txt");

		String fileName = getResources().getString(R.string.file_name);
		String dirName = getResources().getString(R.string.folder_name);
		File arcusDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName );
		File tagCsv = new File(arcusDir.getAbsolutePath() + File.separator + fileName);

		FileOutputStream outputStream;



		Toast toast = Toast.makeText(getBaseContext(), R.string.toast_save_successfully, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);


		try {

			if (!arcusDir.exists()) {
				arcusDir.mkdir();
			}

			JSONObject stockTakeJson = new JSONObject();
			JSONObject stationIdJson = new JSONObject();
			JSONObject tagReadsOutJson = new JSONObject();
			JSONArray tagReadsInJson = new JSONArray();


			stationIdJson.put("account", arcus_account);
			stationIdJson.put("location", arcus_location);
			stationIdJson.put("name", arcus_station_id);


			for (int pos = 0; pos < count; pos++) {

				JSONObject dataJson = new JSONObject();
				JSONObject tagDataJson = new JSONObject();
				JSONObject timestampJson = new JSONObject();
				long epoch = System.currentTimeMillis();
				String tag = adpTags.getItem(pos);
				int length = lengthOfItemID(tag.substring(0, ITEM_ID_START_INDEX));

				/*if(tag.length() < ITEM_ID_START_INDEX + ITEM_ID_LENGTH + 1) {
					break;
				} */

				String itemIdInTag = tag.substring(ITEM_ID_START_INDEX, ITEM_ID_START_INDEX+ length);

				tagDataJson.put("epc", itemIdInTag);
				timestampJson.put("epoch", epoch);
				timestampJson.put("tzoffset", timezone_offset);
				dataJson.put("timestamp", timestampJson);
				dataJson.put("tagData", tagDataJson);

				tagReadsInJson.put(dataJson);
			}

			tagReadsOutJson.put("tagReads", tagReadsInJson);

			stockTakeJson.put("tagReads", tagReadsOutJson);
            stockTakeJson.put("stationId", stationIdJson);

			outputStream = new FileOutputStream(tagCsv, false);
			outputStream.write(stockTakeJson.toString().getBytes());
			outputStream.close();
			clear();
			toast.show();
			btnUpload.setEnabled(true);

		} catch (Exception e) {
			e.printStackTrace();
			toast.setText(R.string.toast_save_failed);
			toast.show();
		}

	}



	private void uploadInventory(View v) {
        String server = getResources().getString(R.string.arcus_server_address);
        String publish = getResources().getString(R.string.arcus_publish_address);
        String account = getResources().getString(R.string.arcus_account);
        String location = getResources().getString(R.string.arcus_location);
        String station = getResources().getString(R.string.arucs_station_id);

		Thread arcus;
		String fileName = getResources().getString(R.string.file_name);
		String dirName = getResources().getString(R.string.folder_name);
		File arcusDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName );
		File inventoryFd = new File(arcusDir.getAbsolutePath() + File.separator + fileName);
		FileInputStream inStream;
		String stockTakeJson;

        int progressStatus = 0;
        uploadingProgress = new ProgressDialog(v.getContext());
        uploadingProgress.setCancelable(true);
        uploadingProgress.setMessage(getResources().getString(R.string.progress_uploading));
        uploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        uploadingProgress.setProgress(progressStatus);
        uploadingProgress.setMax(100);
        //Handler progressBarHandler = new Handler();

		int length = (int) inventoryFd.length();
		byte[] bytesRead = new byte[length];

		Toast toast = Toast.makeText(getBaseContext(), R.string.toast_network_failed, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);

        uploadingProgress.show();

		try {

			inStream = new FileInputStream(inventoryFd);
			inStream.read(bytesRead);
			stockTakeJson = new String(bytesRead);


			arcus = new Thread(new ArcusConnection(server, publish, account, location, station, stockTakeJson, this));
			arcus.start();

			inStream.close();



		} catch (Exception e) {
			Log.e(TAG, "ERROR. uploadInventory() - ", e);
            toast.show();
		}

	}


	private boolean isFileExist() {
		String fileName = getResources().getString(R.string.file_name);
		String dirName = getResources().getString(R.string.folder_name);
		File arcusDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName );
		File inventoryFd = new File(arcusDir.getAbsolutePath() + File.separator + fileName);

		return inventoryFd.exists();
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
		byte[] pcBytes =  hexStringToByteArray(pc.substring(0, ITEM_ID_START_INDEX));
		BitSet pcBitSet = fromByteArray(pcBytes);
		BitSet lenBitSet = pcBitSet.get(11, 16);

		length = (int) bitsetToLong(lenBitSet);

		return length * 4;
	}



	private void startUpdateList() {
		mThread = new Thread(mTimerThread);
		mThread.start();
		Log.i(TAG, "INFO. startUpdateList()");
	}

	private void stopUpdateList() {
		if (mThread == null)
			return;

		mIsAliveThread = false;
		try {
			mThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "ERROR. stopUpdateList() - Failed to join update list thread", e);
		}
		mThread = null;

		Log.i(TAG, "INFO. stopUpdateList()");
	}

	private Runnable mTimerThread = new Runnable() {

		@Override
		public void run() {
			mIsAliveThread = true;
			
			while (mIsAliveThread) {
				runOnUiThread(mUpdateList);
				SysUtil.sleep(UPDATE_TIME);
			}
		}

	};

	private Runnable mUpdateList = new Runnable() {

		@Override
		public void run() {
			synchronized (adpTags) {
				adpTags.notifyDataSetChanged();
			}
		}

	};

    private Runnable mConnectArucs = new Runnable() {
        public void run() {

        }

    };


}
