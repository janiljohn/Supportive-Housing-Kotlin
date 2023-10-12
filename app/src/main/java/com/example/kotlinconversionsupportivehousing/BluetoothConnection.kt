//package com.example.kotlinconversionsupportivehousing
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothGatt
//import android.bluetooth.BluetoothGattCallback
//import android.bluetooth.BluetoothGattCharacteristic
//import android.bluetooth.BluetoothGattDescriptor
//import android.bluetooth.BluetoothGattService
//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothProfile
//import android.bluetooth.le.ScanCallback
//import android.bluetooth.le.ScanResult
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Parcelable
//import android.util.Log
//import android.view.View
//import android.widget.Toast
//import androidx.activity.result.ActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import org.json.JSONException
//import org.json.JSONObject
//import java.nio.charset.StandardCharsets
//import java.util.LinkedList
//import java.util.Queue
//import java.util.UUID
//
//class BluetoothConnection : AppCompatActivity() {
//
//
//    //    ESP-01 UUIDs
////    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
////    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
////    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
//
//    // ESP-02 UUIDs
////    val CHARACTERISTIC_UUID = "83755cbd-e485-4153-ac8b-ce260afd3697"
////    val SERVICE_UUID = "adee10c3-91dd-43aa-ab9b-052eb63d456c"
////    val DESCRIPTOR_UUID = "4d6ec567-93f0-4541-8152-81b35dc5cb8b"
//
//    //    BLANK
//    val CHARACTERISTIC_UUID = "681F827F-D00E-4307-B77A-F38014D6CC5F"
//    val SERVICE_UUID = "3BED005E-75B7-4DE6-B877-EAE81B0FC93F"
//    val DESCRIPTOR_UUID = "013B54B2-5520-406A-87F5-D644AD3E0565"
//
//    val list = listOf<String>(
//        Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
//        Manifest.permission.BLUETOOTH_SCAN,
//        Manifest.permission.BLUETOOTH_CONNECT,
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION)
//
//    var hour = 0;
//    var minute:Int = 0
//
//    val devices = ArrayList<BluetoothDevice>()
//    val uuidMapping: Map<String, Array<Parcelable>> = HashMap()
//    val adapter = BluetoothAdapter.getDefaultAdapter()
//
//
//    val bluetoothScanner = adapter.bluetoothLeScanner
//
//    var device: BluetoothDevice?= null
//
//    var deviceService: BluetoothGattService?= null
//
//    val queue: Queue<String> = LinkedList()
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
//    @SuppressLint("MissingPermission")
//    val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            Log.i("Device Status", newState.toString() + "")
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.i("Device info", "Discovering bluetooth services of target device...")
//                Log.i("Device info", "Connected to ESP32 successfully")
//                gatt.requestMtu(512)
////                    gatt.discoverServices();
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                Log.i("Device info", "Disconnecting bluetooth device...")
//                gatt.disconnect()
//                gatt.close()
//            }
//        }
//
//        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.i("MTU Request", "MTU request success")
//                gatt.discoverServices()
//            } else {
//                Log.i("MTU Request", "MTU request failed")
//            }
//        }
//
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic
//        ) {
//            // Read the updated characteristic value
//            val message = characteristic.value
//            val messageString = String(message, StandardCharsets.UTF_8)
//            Log.i("Unparsed JSON string: ", messageString)
//            var status = ""
//            var lastDetected = 0
//            val motionDetected: Boolean
//            val proximityDetected: Boolean
//            val lightDetected: Boolean
//            val vibrationDetected: Boolean
//            try {
//                val jsonObject = JSONObject(messageString)
//                status = jsonObject.getString("status")
//                lastDetected = jsonObject.getInt("lastDetected")
//                motionDetected = jsonObject.getBoolean("motion")
//                proximityDetected = jsonObject.getBoolean("proximity")
//                lightDetected = jsonObject.getBoolean("light")
//                vibrationDetected = jsonObject.getBoolean("vibration")
//                val finalStatus = status
//                val finalLastDetected = lastDetected
//            } catch (e: JSONException) {
//                Log.i("Error", "Could not parse JSON string")
//            }
//            Log.i("Notification", "Updated status: $status")
//            Log.i("Notification", "Last detected: $lastDetected")
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val service = gatt.getService(UUID.fromString(SERVICE_UUID))
//                deviceService = service
//                val operation = queue.poll()
//                if(operation.equals("sendData")){
//                    sendData(gatt)
//                }
//                Log.i("Device info", "Successfully discovered services of target device")
//                if (service != null) {
//                    Log.i("Service status", "Service is not null.")
//                    val discoveredCharacteristic =
//                        service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
//                    if (discoveredCharacteristic != null) {
//                        gatt.readCharacteristic(discoveredCharacteristic)
//                        if (gatt.setCharacteristicNotification(discoveredCharacteristic, true)) {
//                            Log.i("Set characteristic notification", "Success!")
//                            Log.i(
//                                "Characteristic property flags",
//                                discoveredCharacteristic.properties.toString()
//                            )
//                            val desc = discoveredCharacteristic.getDescriptor(
//                                UUID.fromString(DESCRIPTOR_UUID)
//                            )
//                            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                            gatt.writeDescriptor(desc)
//                            //gatt.requestMtu(512);
//                        } else {
//                            Log.i("Set characteristic notification", "Failure!")
//                        }
//                    } else {
//                        Log.i("Characteristic info", "Characteristic not found!")
//                    }
//                } else {
//                    Log.i("Service info", "Service not found!")
//                }
//            } else {
//                Log.i("Service Discovery", "Service discovery failed")
//            }
//        }
//
//        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val data = discoveredCharacteristic.value
//                val value = String(data, StandardCharsets.UTF_8)
//                Log.i("Read data", "Received data: $value")
//            }
//        }
//
//        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//            }
//        }
//    }
//
//    fun initializeAdapters() {
//        if (adapter == null) {
//            Log.i("initializeAdapters()", "Device does not support Bluetooth")
//            return
//        }
//        if (adapter.isEnabled) {
//            Log.i("initializeAdapters()", "Bluetooth is enabled")
//        } else {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
////            someActivityResultLauncher.launch(enableBtIntent)
//        }
//    }
//
//    open fun scanForBluetooth() {
//        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            } else {
//                requestPermissions( arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_SCAN )
//            }
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
//            createButtons(scannedDevicesList)
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    fun startDeviceDiscovery(operation: String){
//        val gatt = device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//        gatt?.discoverServices()
//        queue.add(operation)
//    }
//
//    @SuppressLint("MissingPermission")
//    fun sendData(gatt: BluetoothGatt){
//
//        val s = "message"
//        val charsetName = "UTF-16"
//        val byteArray = s.toByteArray(StandardCharsets.UTF_8)
//
//        deviceService = gatt?.getService(UUID.fromString(SERVICE_UUID))
//
//        if (deviceService != null) {
//            val example: BluetoothGattCharacteristic = deviceService!!.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
//            //                val example: BluetoothGattCharacteristic = BluetoothGattCharacteristic(UUID.fromString(CHARACTERISTIC_UUID), 8, 16)
//
//            if (example != null) {
//                Log.i("Permission Value", example.permissions.toString() + "")
//                example.value = byteArray
//                gatt?.writeCharacteristic(example)
//
//                //                    Below are write characteristics for API 33
//                //                    gatt?.writeCharacteristic(example, byteArray, 2)
//                //                    device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//                //                        ?.writeCharacteristic(example, byteArray, 2)
//                //                    gatt.writeCharacteristic(example)
//
//                Log.i("Send Data", "the data was sent!")
//            }
//        }
//
//    }
//
//    @SuppressLint("MissingPermission")
//    fun scanForBluetoothWithPermissions(){
//        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
//            showAlert()
//        } else {
//            Log.i("Permission", "Scan Permission granted")
//            bluetoothScanner.startScan(scanCallback)
//        }
//    }
//
//    private fun isPermissionsGranted(): Int {
//        var counter = 0;
//        for (permission in list) {
//            counter += ContextCompat.checkSelfPermission(this, permission)
//        }
//        return counter
//    }
//
//    private fun showAlert() {
//        val builder = android.app.AlertDialog.Builder(this)
//        builder.setTitle("Need permission(s)")
//        builder.setMessage("Bluetooth permissions are required to do the task.")
//        builder.setPositiveButton("OK", { dialog, which -> requestPermissions() })
//        builder.setNeutralButton("Cancel", null)
//        val dialog = builder.create()
//        dialog.show()
//    }
//
//    private fun requestPermissions() {
//        val permission = deniedPermission()
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//            // Show an explanation asynchronously
//            Log.i("requestPermissions()", "Should show an explanation.")
//        } else {
//            ActivityCompat.requestPermissions(this, list.toTypedArray(), permissionrequestcode)
//        }
//    }
//
//    private fun deniedPermission(): String {
//        for (permission in list) {
//            if (ContextCompat.checkSelfPermission(this, permission)
//                == PackageManager.PERMISSION_DENIED) return permission
//        }
//        return ""
//    }
//
//    fun processPermissionsResult(requestCode: Int, permissions: Array<String>,
//                                 grantResults: IntArray): Boolean {
//        var result = 0
//        if (grantResults.isNotEmpty()) {
//            for (item in grantResults) {
//                result += item
//            }
//        }
//        if (result == PackageManager.PERMISSION_GRANTED) return true
//        return false
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        when (requestCode) {
//            permissionrequestcode -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.i("Permission", "Permissions already granted.")
//                    bluetoothScanner.startScan(scanCallback)
//                } else {
//                    showAlert()
//                    // Permission denied, handle accordingly (e.g., show a message or disable functionality)
//                }
//            }
//        }
//    }
//
//}