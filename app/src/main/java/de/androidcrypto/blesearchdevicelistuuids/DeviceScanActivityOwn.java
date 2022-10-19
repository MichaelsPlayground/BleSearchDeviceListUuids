package de.androidcrypto.blesearchdevicelistuuids;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DeviceScanActivityOwn extends AppCompatActivity {

    // https://developer.android.com/guide/topics/connectivity/bluetooth/find-ble-devices#java

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private boolean scanning;
    private Handler handler = new Handler();

    ProgressBar progressBar;
    Button scan, btnReturn;
    ListView listView;
    ArrayAdapter<String> scannedDevicesArrayAdapter;
    List<String> subject_list; // for temporary list

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000; // 5000 = 5 seconds

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan_own);

        progressBar = findViewById(R.id.pbList);
        listView = findViewById(R.id.lvListListView);
        scan = findViewById(R.id.btnListScan);
        btnReturn = findViewById(R.id.btnListReturn);

        // populate the data
        scannedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        listView.setAdapter(scannedDevicesArrayAdapter);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scannedDevicesArrayAdapter.clear();
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.VISIBLE);
                //doDiscovery();
                subject_list = new ArrayList<String>();
                scanLeDevice();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.GONE);
                System.out.println("This item was clicked: " + i);
                // Cancel discovery because it's costly and we're about to connect
                //mBtAdapter.cancelDiscovery();
                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                // check for the text "scanning complete"
                if (address.equalsIgnoreCase("scanning complete")) {
                    System.out.println("do not use this data");
                    address = "";
                }
                System.out.println("*** MAC: " + address);

                // Create the Intent and include the MAC address
                Intent intent = new Intent(DeviceScanActivityOwn.this, MainActivity.class);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);
                finish();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.GONE);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    String deviceInfos =
                            "name: " + result.getDevice().getName()
                            + " type: " + result.getDevice().getType()
                            + " address: " + result.getDevice().getAddress();
                    // todo make a switch if all devices or only named devices get added
                    //if (result.getDevice().getName() != null) {
                        // this code is for avoiding duplicates in the listview
                        subject_list.add(deviceInfos);
                        HashSet<String> hashSet = new HashSet<String>();
                        hashSet.addAll(subject_list);
                        subject_list.clear();
                        subject_list.addAll(hashSet);
                        scannedDevicesArrayAdapter.clear();
                        scannedDevicesArrayAdapter.addAll(hashSet);
                    //scannedDevicesArrayAdapter.add(deviceInfos);

                    System.out.println("\nMAC: " + result.getDevice().getAddress());
                    List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
                    if (uuids != null) {
                        int uuidsSize = uuids.size();
                        System.out.println("Found UUIDs: " + uuidsSize);
                        for (int i = 0; i < uuidsSize; i++) {
                            System.out.println("UUID " + i + " : " + uuids.get(i).getUuid().toString());
                        }
                    } else {
                        System.out.println("no UUID(s) available");
                    }
                    System.out.println("------------------------------");
                    //}
                }
            };

            // 0000fd6f-0000-1000-8000-00805f9b34fb = Corona Warn App
}