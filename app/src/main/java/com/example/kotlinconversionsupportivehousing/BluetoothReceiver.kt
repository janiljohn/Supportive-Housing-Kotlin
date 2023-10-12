package com.example.kotlinconversionsupportivehousing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class BluetoothReceiver : BroadcastReceiver() {


//    val devices = ArrayList<BluetoothDevice>()
//    val adapter = BluetoothAdapter.getDefaultAdapter()
//    val bluetoothScanner = adapter.bluetoothLeScanner
//
//    val scanCallback: ScanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            Log.i("Scan passed", "Could  complete scan for nearby BLE devices")
//            devices.add(result.device)
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            Log.i("Scan failed", "Could not complete scan for nearby BLE devices")
//            return
//        }
//    }
//
//    open fun scanForBluetooth() {
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
////            } else {
////                requestPermissions( arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_SCAN )
////            }
//        } else {
//            bluetoothScanner.startScan(scanCallback)
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    open fun startProcess() {
//        var device: BluetoothDevice? = null
//        var targetDeviceAddress = ""
//        val manager = getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
//        val connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
//        val scannedDevicesList = java.util.ArrayList<BluetoothDevice>()
//        var notFound = true
//        while (connectedDevices.isEmpty() && notFound) {
//            if (!devices.isEmpty()) {
//                val deviceSize = devices.size
//                for (i in 0 until deviceSize) {
//                    if (devices[i] != null && devices[i].name != null) {
//                        targetDeviceAddress = devices[i].name
//                        device = devices[i]
//                        Log.i("Device Found", "Found target device: " + device.name)
//                        Log.i("Device Address", "Device address is: $targetDeviceAddress")
//                        scannedDevicesList.add(devices[i])
//                        bluetoothScanner.stopScan(scanCallback)
//                        notFound = false
////                            break
//                        /*if (devices.get(i).getName().equals("ESP32")) {
//                        targetDeviceAddress = devices.get(i).getName();
//                        device = devices.get(i);
//                        Log.i("Device Found", "Found target device: " + device.getName());
//                        Log.i("Device Address", "Device address is: " + targetDeviceAddress);
//                        scannedDevicesList.add(devices.get(i));
//                        bluetoothScanner.stopScan(scanCallback);
//                        notFound = false;
//                        break;
//                    }*/
//                    }
//                }
//                if (device == null) {
//                    Log.i("Devices", "Target device was not found")
//                    return
//                }
//            } else {
//                Log.i("Devices", "No devices were found")
//                return
//            }
//        }
//    }


//    var mainActivityObj : MainActivity
    lateinit var mainActivityObj : MainActivity
    override fun onReceive(context: Context?, intent: Intent?) {
//        val action: String = intent!!.action!!
//
//        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
//            // Scanning started
//        } else if (BluetoothDevice.ACTION_FOUND == action) {
//            // Device found
//            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
//            // Process the device
//
//            mainActivityObj.autoConnect()
//        }


        Log.d("AlarmReceiver", "Repeating alarm triggered!")

    }

    fun test(startAutoConnect: AutoConnect){

        startAutoConnect.autoConnect()

    }
}