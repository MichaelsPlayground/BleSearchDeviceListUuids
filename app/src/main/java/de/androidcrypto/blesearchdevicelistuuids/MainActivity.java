package de.androidcrypto.blesearchdevicelistuuids;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button scanForDevices, getDeviceData;
    com.google.android.material.textfield.TextInputEditText connectedDevice;
    com.google.android.material.textfield.TextInputEditText dataOfDevice;

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    String macAddressFromScan = ""; // will get filled by Intent from DeviceScanActivity

    /**
     * This block is for requesting permissions up to Android 12+
     *
     */

    private static final int PERMISSIONS_REQUEST_CODE = 191;
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @SuppressLint("InlinedApi")
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectedDevice = findViewById(R.id.etMainConnectedDevice);
        dataOfDevice = findViewById(R.id.etMainDataOfDevice);

        requestBlePermissions(this, PERMISSIONS_REQUEST_CODE);

        scanForDevices = findViewById(R.id.btnMainScan);
        scanForDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, DeviceScanActivityOwn.class);
                startActivity(intent);
                /*
                BluetoothHandler bth = BluetoothHandler.getInstance(view.getContext());
                BluetoothCentralManager bcm = bth.central;
                bcm.stopScan();
                bcm.close();
                registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
                registerReceiver(heartRateDataReceiver, new IntentFilter( BluetoothHandler.MEASUREMENT_HEARTRATE ));

                bcm.scanForPeripherals();
                */
            }
        });

        // receive the address from DeviceListOwnActivity, if we receive data run the connection part
        Intent incommingIntent = getIntent();
        Bundle extras = incommingIntent.getExtras();
        if (extras != null) {
            macAddressFromScan = extras.getString(EXTRA_DEVICE_ADDRESS); // retrieve the data using keyName
            System.out.println("Main received data: " + macAddressFromScan);
            try {
                if (!macAddressFromScan.equals("")) {
                    Log.i("Main", "selected MAC: " + macAddressFromScan);
                    connectedDevice.setText(macAddressFromScan);
                }
            } catch (NullPointerException e) {
                // do nothing, there are just no data
            }
        }

        Button getDeviceData = findViewById(R.id.btnMainGetDeviceData);
        getDeviceData.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                String macAddress = connectedDevice.getText().toString();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
                String data = "";
                data += "MAC: " + bluetoothDevice.getAddress() + "\n";
                data += "Name: " + bluetoothDevice.getName() + "\n";
                data += "Type: " + bluetoothDevice.getType() + "\n";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    data += "Alias: " + bluetoothDevice.getAlias() + "\n";
                }
                ParcelUuid[] uuids = bluetoothDevice.getUuids();
                if (uuids != null) {
                    int uuidsSize = uuids.length;
                    data += "Found UUIDs: " + uuidsSize + "\n";
                    for (int i = 0; i < uuidsSize; i++) {
                        data += "UUID " + i + " : " + uuids[i].getUuid().toString() + "\n";
                    }
                } else {
                    data += "no UUID(s) available";
                }

                dataOfDevice.setText(data);
            }
        });
    }
}