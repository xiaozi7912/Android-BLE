package com.xiaozi.androidble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends BaseActivity {
    private BluetoothAdapter mBTAdapter = null;
    private BluetoothLeScanner mLeScanner = null;
    private Timer mDiscoveryTimer = null;

    private Button mServerButton = null;
    private Button mAdvertiseButton = null;

    private final int DISCOVERY_DEVICE_TIME = 5 * 1000;
    private final String MY_UUID = "8ee1d244-8e71-4555-86e1-f320c81d2c38";
    private final static int REQUEST_RUNTIME_PERMISSION_CODE = 1000;
    private final static String[] RUNTIME_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "onReceive");
            String action = intent.getAction();
            Log.d(LOG_TAG, "onReceive action : " + action);
            Log.d(LOG_TAG, "onReceive intent : " + intent);

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRuntimePermission();
        initReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RUNTIME_PERMISSION_CODE:
                checkRuntimePermission();
                break;
        }
    }

    private void checkRuntimePermission() {
        int hasReadExternalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int hasWriteExternalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((hasReadExternalStorage != PackageManager.PERMISSION_GRANTED) || (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED)) {
            boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (showRequestPermission) {
            } else {
                ActivityCompat.requestPermissions(mActivity, RUNTIME_PERMISSIONS, REQUEST_RUNTIME_PERMISSION_CODE);
            }
        } else {
        }
    }

    private void initView() {
        mServerButton = (Button) findViewById(R.id.main_start_server_button);
        mAdvertiseButton = (Button) findViewById(R.id.main_start_advertise_button);

        if (!mBTAdapter.isMultipleAdvertisementSupported()) {
            mAdvertiseButton.setEnabled(false);
        }

        mServerButton.setOnClickListener(onClickListener);
        mAdvertiseButton.setOnClickListener(onClickListener);
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void onServerButtonClicked() {
        Log.i(LOG_TAG, "onServerButtonClicked");
        if (mBTAdapter.isEnabled()) {
            mLeScanner = mBTAdapter.getBluetoothLeScanner();
            List<ScanFilter> filters = new ArrayList<ScanFilter>();
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(MY_UUID));
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(pUuid).build();
            filters.add(filter);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mLeScanner.startScan(filters, settings, mScanCallback);

            if (mDiscoveryTimer == null) mDiscoveryTimer = new Timer();
            mDiscoveryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mLeScanner.stopScan(mScanCallback);
                }
            }, DISCOVERY_DEVICE_TIME);
        } else {
            mBTAdapter.enable();
        }
    }

    private void onAdvertiseButtonClicked() {
        Log.i(LOG_TAG, "onAdvertiseButtonClicked");
        if (mBTAdapter.isEnabled()) {
            BluetoothLeAdvertiser advertiser = mBTAdapter.getBluetoothLeAdvertiser();
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false).build();
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(MY_UUID));
            AdvertiseData data = new AdvertiseData.Builder()
//                    .setIncludeDeviceName(true)
                    .addServiceUuid(pUuid)
                    .addServiceData(pUuid, "a".getBytes())
                    .build();
            AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.i(LOG_TAG, "onAdvertiseButtonClicked onStartSuccess");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.i(LOG_TAG, "onAdvertiseButtonClicked onStartFailure");
                    Log.d(LOG_TAG, "onAdvertiseButtonClicked onStartFailure errorCode : " + errorCode);
                }
            };
            advertiser.startAdvertising(settings, data, advertisingCallback);
        } else {
            mBTAdapter.enable();
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(LOG_TAG, "onScanResult onScanResult callbackType : " + callbackType);
            Log.d(LOG_TAG, "onScanResult onScanResult result : " + result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(LOG_TAG, "onScanResult onScanFailed errorCode : " + errorCode);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBTAdapter != null) {
                switch (v.getId()) {
                    case R.id.main_start_server_button:
                        onServerButtonClicked();
                        break;
                    case R.id.main_start_advertise_button:
                        onAdvertiseButtonClicked();
                        break;
                }
            } else {
                Toast.makeText(mActivity, "No Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
