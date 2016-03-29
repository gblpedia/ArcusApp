package au.com.adilamtech.app.arcus;

import java.util.Locale;

import com.atid.app.rfid.dialog.WaitDialog;
import com.atid.app.rfid.view.InventoryActivity;
import com.atid.app.rfid.view.LockMemoryActivity;
import com.atid.app.rfid.view.OptionActivity;
import com.atid.app.rfid.view.ReadMemoryActivity;
import com.atid.app.rfid.view.WriteMemoryActivity;
import com.atid.app.rfid.view.base.AccessActivity;
import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.system.ModuleControl;
import com.atid.lib.util.SysUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("Wakelock")
public class MainActivity extends Activity implements OnClickListener,
		RfidReaderEventListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String APP_NAME = "ATRfidDemo";

	private static final int INVENTORY_VIEW = 0;
	private static final int INVENTORY_EX_VIEW = 1;
	private static final int READ_MEMORY_VIEW = 2;
	private static final int WRITE_MEMORY_VIEW = 3;
	private static final int LOCK_MEMORY_VIEW = 4;
	private static final int OPTION_VIEW = 5;
    private static final int FIND_ITEM_VIEW = 6;

	// ------------------------------------------------------------------------
	// Member Variable
	// ------------------------------------------------------------------------

	private ATRfidReader mReader = null;

	private TextView txtFirmwareVersion;
	private Button btnFindItem;
	private Button btnInventory;
	private Button btnReadMemory;
	private Button btnWriteMemory;
	private Button btnLockMemory;
	private Button btnOption;
	private ImageView imgLogo;

	// ------------------------------------------------------------------------
	// Main Activit Event Handler
	// ------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Initialize Widgets
		initWidgets();

		// Setup always wake up
		SysUtil.wakeLock(this, APP_NAME);

		WaitDialog.show(this, R.string.connect_module, new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				MainActivity.this.finish();
			}
			
		});

		if ((mReader = ATRfidManager.getInstance()) == null) {
			WaitDialog.hide();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.module_error);
			builder.setMessage(R.string.fail_check_module);
			builder.setPositiveButton(R.string.action_ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}

					});
			builder.show();
		}

		Log.i(TAG, "INFO. onCreate()");
	}

	@Override
	protected void onDestroy() {

		// Deinitalize RFID reader Instance
		ATRfidManager.onDestroy();

		// Wake Unlock
		SysUtil.wakeUnlock();

		Log.d(TAG, "INFO. onDestroy");

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mReader != null) {
			ATRfidManager.wakeUp();
		}

		Log.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATRfidManager.sleep();

		Log.i(TAG, "INFO. onStop()");

		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mReader != null)
			mReader.setEventListener(this);

		Log.d(TAG, "INFO onResume()");
	}

	@Override
	protected void onPause() {

		if (mReader != null)
			mReader.removeEventListener(this);

		Log.i(TAG, "INFO. onPause()");
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;

		enableMenuButtons(false);

		switch (v.getId()) {

        case R.id.find_tag:
            intent = new Intent(this, FindTagActivity.class);
            startActivityForResult(intent, FIND_ITEM_VIEW);
            break;
		case R.id.inventory:
			intent = new Intent(this, InventoryActivity.class);
			startActivityForResult(intent, INVENTORY_VIEW);
			break;
		/*case R.id.read_memory:
			intent = new Intent(this, ReadMemoryActivity.class);
//			intent.putExtra(AccessActivity.KEY_SELECTED_TAG, false);
			startActivityForResult(intent, READ_MEMORY_VIEW);
			break;
		case R.id.write_memory:
			intent = new Intent(this, WriteMemoryActivity.class);
//			intent.putExtra(AccessActivity.KEY_SELECTED_TAG, false);
			startActivityForResult(intent, WRITE_MEMORY_VIEW);
			break;
		case R.id.lock_memory:
			intent = new Intent(this, LockMemoryActivity.class);
//			intent.putExtra(AccessActivity.KEY_SELECTED_TAG, false);
			startActivityForResult(intent, LOCK_MEMORY_VIEW);
			break;
		case R.id.option:
			intent = new Intent(this, OptionActivity.class);
			startActivityForResult(intent, OPTION_VIEW);
			break;*/
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
        case FIND_ITEM_VIEW:
		case INVENTORY_VIEW:
		case INVENTORY_EX_VIEW:
		case READ_MEMORY_VIEW:
		case WRITE_MEMORY_VIEW:
		case LOCK_MEMORY_VIEW:
		case OPTION_VIEW:
			enableMenuButtons(true);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// ------------------------------------------------------------------------
	// Reader Event Handler
	// ------------------------------------------------------------------------

	@Override
	public void onReaderStateChanged(ATRfidReader reader, ConnectionState state) {

		switch (state) {
		case Connected:
			WaitDialog.hide();
			enableMenuButtons(true);
			String version = "";
			try {
				version = mReader.getFirmwareVersion();
			} catch (ATRfidReaderException e) {
				Log.e(TAG,
						String.format(Locale.US, 
								"ERROR. onReaderStateChanged(%s) - Failed to get firmware version [%s]",
								state, e.getCode()), e);
				version = "";
				mReader.disconnect();
			}
			txtFirmwareVersion.setText(version);
			imgLogo.setImageResource(R.drawable.ic_connected_logo);
			break;
		case Disconnected:
			WaitDialog.hide();
			enableMenuButtons(false);
			imgLogo.setImageResource(R.drawable.ic_disconnected_logo);
			break;
		case Connecting:
			enableMenuButtons(false);
			imgLogo.setImageResource(R.drawable.ic_connecting_logo);
			break;
		default:
			break;
		}

		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderStateChanged(%s)", state));
	}

	@Override
	public void onReaderActionChanged(ATRfidReader reader, ActionState action) {
		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderActionchanged(%s)", action));
	}

	@Override
	public void onReaderReadTag(ATRfidReader reader, String tag, float rssi) {
		Log.i(TAG,
				String.format(Locale.US, "EVENT. onReaderReadTag([%s], %.2f)", tag, rssi));
	}

	@Override
	public void onReaderResult(ATRfidReader reader, ResultCode code,
			ActionState action, String epc, String data) {
		Log.i(TAG, String.format(Locale.US, "EVENT. onReaderResult(%s, %s, [%s], [%s]",
				code, action, epc, data));
	}

	// ------------------------------------------------------------------------
	// Internal Widget Control Methods
	// ------------------------------------------------------------------------

	// Initialize Main Activity Widgets
	private void initWidgets() {
		String version = SysUtil.getVersion(this);


		txtFirmwareVersion = (TextView) findViewById(R.id.firmware_version);
        btnFindItem = (Button) findViewById(R.id.find_tag);
        btnFindItem.setOnClickListener(this);
		btnInventory = (Button) findViewById(R.id.inventory);
		btnInventory.setOnClickListener(this);
		/*btnReadMemory = (Button) findViewById(R.id.read_memory);
		btnReadMemory.setOnClickListener(this);
		btnWriteMemory = (Button) findViewById(R.id.write_memory);
		btnWriteMemory.setOnClickListener(this);
		btnLockMemory = (Button) findViewById(R.id.lock_memory);
		btnLockMemory.setOnClickListener(this);
		btnOption = (Button) findViewById(R.id.option);
		btnOption.setOnClickListener(this);*/
		imgLogo = (ImageView) findViewById(R.id.app_logo);
	}

	// Enable/Disable Menu Button
	private void enableMenuButtons(boolean enabled) {
        btnFindItem.setEnabled(enabled);
		btnInventory.setEnabled(enabled);
		/*btnReadMemory.setEnabled(enabled);
		btnWriteMemory.setEnabled(enabled);
		btnLockMemory.setEnabled(enabled);
		btnOption.setEnabled(enabled);*/
	}
}
