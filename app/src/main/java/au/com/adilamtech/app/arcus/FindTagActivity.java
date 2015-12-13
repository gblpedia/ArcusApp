package au.com.adilamtech.app.arcus;

import android.content.Intent;
import android.drm.DrmStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.atid.app.rfid.GlobalInfo;
import com.atid.app.rfid.adapter.TagListAdapter;
import com.atid.app.rfid.view.LockMemoryActivity;
import com.atid.app.rfid.view.ReadMemoryActivity;
import com.atid.app.rfid.view.WriteMemoryActivity;
import com.atid.app.rfid.view.base.AccessActivity;
import com.atid.app.rfid.view.base.ActionActivity;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.SelectionMask6c;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.InventorySession;
import com.atid.lib.dev.rfid.type.InventoryTarget;
import com.atid.lib.dev.rfid.type.MaskActionType;
import com.atid.lib.dev.rfid.type.MaskTargetType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.dev.rfid.type.TagType;
import com.atid.lib.util.SysUtil;

import java.util.Locale;

public class FindTagActivity extends ActionActivity implements OnCheckedChangeListener {

	private static final int READ_MEMORY_ACTIVITY = 1;
	private static final int WRITE_MEMORY_ACTIVITY = 2;
	private static final int LOCK_MEMORY_ACTIVITY = 3;


	private static final int UPDATE_TIME = 500;
    private static final int ITEM_ID_START_INDEX = 4;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private EditText inputItemId;
    private TextView finding_progress;
	private Button btnFind;

    private boolean isContinuous = true;
	private boolean mIsReportRssi;

	private Thread mThread;
	private boolean mIsAliveThread;
    private String mItemIdInHex;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public FindTagActivity() {
		super();

		TAG = FindTagActivity.class.getSimpleName();
		mView = R.layout.activity_find_tag;

		mIsReportRssi = false;

		mThread = null;
		mIsAliveThread = false;
	}



    private String asciiToHex(String ascii) {

        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < ascii.length(); i++) {

            hex.append(Integer.toHexString(ascii.charAt(i)));
        }

        return hex.toString();
    }

	// ------------------------------------------------------------------------
	// Activity Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        /*
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
		*/
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		/*if (v.getId() == R.id.tag_list && mReader.getAction() == ActionState.Stop) {
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
		*/
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		/*Intent intent;
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
		*/
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

        String session;
        String target;


        try {
            session = reader.getInventorySession().toString();
            target = reader.getInventoryTarget().toString();

        } catch(Exception e) {
            session = "Null";
            target = "Null";
        }

		runOnUiThread(new Runnable() {

            @Override
            public void run() {
				/*synchronized (adpTags) {
					adpTags.addItem(tag, rssi);
				}
				txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));*/
                String itemIdInTag = tag.substring(ITEM_ID_START_INDEX, ITEM_ID_START_INDEX + mItemIdInHex.length());

                if( itemIdInTag.equals(mItemIdInHex) ) {
                    playSuccess();
                    Log.i(TAG, String.format(Locale.US, "EVENT. onReaderReadTag(playSuccess(%s)", itemIdInTag));
                }
            }

        });

		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderReadTag([%s], %.2f, %s, %s)", tag, rssi, session, target));
	}
	
	// ------------------------------------------------------------------------
	// Reader Control Methods
	// ------------------------------------------------------------------------

	// Start Action
	protected void startAction() {

		ResultCode res;
		TagType tagType = getTagType();
        mItemIdInHex = asciiToHex(inputItemId.getText().toString());

		enableWidgets(false);
		startUpdateList();

		if (isContinuous) {
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
        finding_progress.setText("");
		Log.i(TAG, "INFO. clear()");
	}

	// Initialize Activity Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

        inputItemId = (EditText) findViewById(R.id.itemId_input);
        inputItemId.setText("000004");    //E20068060F017B0F
        finding_progress = (TextView) findViewById(R.id.finding_progress);
        finding_progress.setMovementMethod(new ScrollingMovementMethod());

		// Action Button
		btnFind = (Button) findViewById(R.id.action);
		btnFind.setOnClickListener(this);

		Log.i(TAG, "INFO. initWidgets()");
	}

	// Enable Activity Widgets
	@Override
	protected void enableWidgets(boolean enabled) {
		super.enableWidgets(enabled);

		if (mReader.getAction() == ActionState.Stop) {

			btnFind.setText(R.string.action_find_tag);
		} else {

			btnFind.setText(R.string.action_stop);
		}
		btnFind.setEnabled(enabled);
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();

		// Get Report RSSI
		try {
			mIsReportRssi = mReader.getReportRssi();
            /*SelectionMask6c mask = new SelectionMask6c(MaskTargetType.S2, MaskActionType.Assert_Deassert, BankType.EPC, 8, 8, "AA",  false);
            mReader.setSelectionMask6c(0, mask);
            mReader.setUseSelectionMask(true);*/
            mReader.setInventoryTarget(InventoryTarget.All);

		} catch (ATRfidReaderException e) {
			Log.e(TAG, String.format(Locale.US, "ERROR. initReader() - Failed to get report RSSI [%s]", e.getCode()),
					e);
		}
		Log.i(TAG, String.format(Locale.US, "INFO. initReader() - [Report RSSI : %s]", mIsReportRssi));

		Log.i(TAG, "INFO initReader()");
	}

	// Activated Reader
	@Override
	protected void activateReader() {
		super.activateReader();

		/*chkDisplayPc.setChecked(GlobalInfo.isDisplayPc());
		chkContinuousMode.setChecked(GlobalInfo.isContinuousMode());
		chkReportRssi.setChecked(mIsReportRssi);
		adpTags.setDisplayPc(GlobalInfo.isContinuousMode());
		adpTags.setVisibleRssi(chkReportRssi.isChecked());*/

		enableWidgets(true);

		Log.i(TAG, "INFO. activateReader()");
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
			/*synchronized (adpTags) {
				adpTags.notifyDataSetChanged();
			}*/
		}

	};
}
