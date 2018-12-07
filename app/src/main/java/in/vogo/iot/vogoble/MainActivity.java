package in.vogo.iot.vogoble;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@TargetApi(21)
public class MainActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 10000;
    public static final String MAC_ADDRESS = "0C:B2:B7:7B:C5:12";
    BluetoothGattCharacteristic characteristic;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mConnectionState = STATE_DISCONNECTED;
    Appdata appdata;

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i("BLE", "Connected to GATT server.");
                    Log.i("BLE", "Attempting to start service discovery:" +
                            gatt.discoverServices());
                    Log.i("gattCallback", "STATE_CONNECTED");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i("BLE", "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService services = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            Log.i("onServicesDiscovered", services.toString());
            characteristic = services.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
            notifyService(true);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", new String(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("onCharacteristic", new String(characteristic.getValue()));
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH
    };
    Button toggle;
    boolean isunlocked = true;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private ScanCallback mScanCallback;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appdata = new Appdata();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference("bleData");
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        toggle = (Button) findViewById(R.id.btn_major);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        toggle.setEnabled(false);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isunlocked){
                    isunlocked = false;
                    toggle.setText("Lock");
                    send("1".getBytes());
                }
                else {
                    isunlocked = true;
                    toggle.setText("Unlock");
                    //send("0".getBytes());
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_GATT_CONNECTED);
            intentFilter.addAction(ACTION_DATA_AVAILABLE);
            intentFilter.addAction(ACTION_GATT_DISCONNECTED);
            intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
            intentFilter.addAction(EXTRA_DATA);
            registerReceiver(mGattUpdateReceiver, intentFilter);
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();

                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        Log.i("callbackType", String.valueOf(callbackType));
                        Log.i("result", result.toString());
                        BluetoothDevice btDevice = result.getDevice();
                        Log.d("device", btDevice.getAddress());
                        if (btDevice.getAddress().equalsIgnoreCase(MAC_ADDRESS)) {
                            connectToDevice(btDevice);
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult sr : results) {
                            Log.i("ScanResult - Results", sr.toString());
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.e("Scan Failed", "Error Code: " + errorCode);
                    }
                };
            }
            else{
                  mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi,
                                         byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                if (device.getAddress().equalsIgnoreCase("0C:B2:B7:7B:C5:12")) {
                                    connectToDevice(device);
                                }
                            }
                        });
                    }
                };
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Toast.makeText(this,"Searching",Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            Toast.makeText(this,"Searching Stopped",Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    public boolean send(byte[] data) {
        if (mGatt == null || characteristic == null) {
            Log.w("BLE", "BluetoothGatt not initialized");
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            return false;
        }

        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return mGatt.writeCharacteristic(characteristic);
    }

    public boolean notifyService(boolean yes) {
        if (mGatt == null || characteristic == null) {
            Log.w("BLE", "BluetoothGatt not initialized");
            return false;
        }


        if (characteristic == null) {
            Log.w("BLE", "Send characteristic not found");
            return false;
        }
        Log.d("Charac",String.valueOf(characteristic.getDescriptors().size()));
        for(BluetoothGattDescriptor d:characteristic.getDescriptors()){
            Log.d("BT",d.getUuid().toString());
            d.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            break;
        }
        return mGatt.setCharacteristicNotification(characteristic,yes);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data));

        }
        sendBroadcast(intent);
    }

    public void close() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
                toggle.setEnabled(true);
                appdata.setDeviceConnected(true);
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(),"DisConnected",Toast.LENGTH_LONG).show();
                toggle.setEnabled(false);
            }  else if (ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(EXTRA_DATA));
            }
            else if(ACTION_GATT_SERVICES_DISCOVERED.equals(action)){

                Toast.makeText(getApplicationContext(),"Service Found",Toast.LENGTH_LONG).show();
                appdata.setServiceDiscovered(true);
                appdata.setTimeStamp(new Date().toString());
                StringBuffer infoBuffer = new StringBuffer();
                infoBuffer.append("Model :" + Build.MODEL + "\n");//The end-user-visible name for the end product.
                infoBuffer.append("Device: " + Build.DEVICE + "\n");//The name of the industrial design.
                infoBuffer.append("Manufacturer: " + Build.MANUFACTURER + "\n");//The manufacturer of the product/hardware.
                infoBuffer.append("Board: " + Build.BOARD + "\n");//The name of the underlying board, like "goldfish".
                infoBuffer.append("Brand: " + Build.BRAND + "\n");//The consumer-visible brand with which the product/hardware will be associated, if any.
                infoBuffer.append("Serial: " + Build.SERIAL + "\n");
                appdata.setDeviceName(infoBuffer.toString());

                databaseReference.push().setValue(appdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"data stored in firebase",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("BLE",e.getMessage());

                    }
                });
            }
        }
    };

    private void displayData(String stringExtra) {

       if(stringExtra.equalsIgnoreCase("4")){
           Toast.makeText(this,stringExtra,Toast.LENGTH_LONG).show();
           Log.d("BLE", "long Pressed");
           showDialog();
       }
       else{
           Toast.makeText(this,stringExtra,Toast.LENGTH_SHORT).show();
       }
    }


    private void showDialog(){
        String[] actions = {"Pause Ride","End Ride"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What you like to do").setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    send("5".getBytes());
                }
                else if(which==1){
                    send("6".getBytes());
                }

                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
