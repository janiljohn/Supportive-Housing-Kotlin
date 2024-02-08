package com.example.kotlinconversionsupportivehousing

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager2.widget.ViewPager2
import com.example.kotlinconversionsupportivehousing.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import org.json.JSONException
import java.nio.charset.StandardCharsets
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import java.util.UUID
import java.util.concurrent.Semaphore

class MainActivity : AppCompatActivity(), AutoConnect, IBackgroundScan {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var REQUEST_BLUETOOTH_SCAN = 3
    val REQUEST_ENABLE_BT = 1
    val REQUEST_ENABLE_BLUETOOTH_ADMIN = 2

//    ESP-01 UUIDs
    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    // ESP-02 UUIDs
//    val CHARACTERISTIC_UUID = "83755cbd-e485-4153-ac8b-ce260afd3697"
//    val SERVICE_UUID = "adee10c3-91dd-43aa-ab9b-052eb63d456c"
//    val DESCRIPTOR_UUID = "4d6ec567-93f0-4541-8152-81b35dc5cb8b"

//    BLANK
//    val CHARACTERISTIC_UUID = "681F827F-D00E-4307-B77A-F38014D6CC5F"
//    val SERVICE_UUID = "3BED005E-75B7-4DE6-B877-EAE81B0FC93F"
//    val DESCRIPTOR_UUID = "013B54B2-5520-406A-87F5-D644AD3E0565"

//    Pill Dispenser
//    val CHARACTERISTIC_UUID = "B3E39CF1-B4D5-4F0A-88DE-6EDE9ABE2BD2"
//    val SERVICE_UUID = "B3E39CF0-B4D5-4F0A-88DE-6EDE9ABE2BD2"
//    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    val connectionSemaphore = Semaphore(1)
    private val permissionrequestcode = 123

        private interface MessageConstants {
            companion object {
                const val MESSAGE_READ = 0
                const val MESSAGE_WRITE = 1
                const val MESSAGE_TOAST = 2
            }
        }


    private var startBackgroundScan: Intent? = null

    lateinit var initializeBluetooth: Button
    lateinit var scanForBluetooth: Button
    lateinit var startBtn: Button
    lateinit var stopBtn: Button
    lateinit var timeButton: Button
    lateinit var textView: TextView
    lateinit var lastDetection: TextView
    lateinit var displayNotification : TextView
    lateinit var context: Context
    lateinit var sendDataBtn: Button
    lateinit var setTime: Button
    lateinit var startServiceBtn: Button
    var schedule = mutableMapOf("Sunday" to "N/A", "Monday" to "N/A", "Tuesday" to "N/A", "Wednesday" to "N/A", "Thursday" to "N/A", "Friday" to "N/A", "Saturday" to "N/A")

    lateinit var setSunday: Button
    lateinit var setMonday: Button
    lateinit var setTuesday: Button
    lateinit var setWednesday: Button
    lateinit var setThursday: Button
    lateinit var setFriday: Button
    lateinit var setSaturday: Button


    lateinit var buttonContainer: LinearLayout
    val list = listOf<String>(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.USE_EXACT_ALARM)

        var hour = 0;
        var minute:Int = 0

        val devices = ArrayList<BluetoothDevice>()
        val uuidMapping: Map<String, Array<Parcelable>> = HashMap()
    val adapter = BluetoothAdapter.getDefaultAdapter()


    val bluetoothScanner = adapter.bluetoothLeScanner

    var device:BluetoothDevice ?= null

    var deviceService:BluetoothGattService ?= null

    var deviceGatt:BluetoothGatt ?= null

    val queue: Queue<String> = LinkedList()

    private var serviceInstance: BackgroundScan? = null

//    private var serviceScannedDevices: kotlin.collections.MutableList<MyBluetoothDevice> = java.util.ArrayList<MyBluetoothDevice>()
//    val serviceScannedDevices = kotlin.collections.List<MyBluetoothDevice>
    val serviceScannedDevices: MutableList<MyBluetoothDevice> = mutableListOf()

    //    private var deviceNameMapping: kotlin.collections.MutableMap<kotlin.String, kotlin.collections.MutableList<MyBluetoothDevice?>> = java.util.HashMap<kotlin.String, kotlin.collections.MutableList<MyBluetoothDevice>>()
//    val deviceNameMapping = kotlin.collections.MutableMap<String, List<MyBluetoothDevice>>
    val deviceNameMapping: MutableMap<String, MutableList<MyBluetoothDevice>> = mutableMapOf()

    val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Log.i("Scan passed", "Could  complete scan for nearby BLE devices")
                devices.add(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                Log.i("Scan failed", "Could not complete scan for nearby BLE devices")
                return
            }
        }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: BackgroundScan.MyBinder = service as BackgroundScan.MyBinder
            serviceInstance = binder.getInstance()
            serviceInstance?.registerClient(this@MainActivity)
            Log.i("SERVICE BIND", "Success")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i("SERVICE BIND", "Disconnected")
        }
    }

    @SuppressLint("MissingPermission")
    val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Log.i("Pill Dispenser", "onConnectionStateChange invoked")
                Log.i("Device Status", newState.toString() + "")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("Device info", "Discovering bluetooth services of target device...")
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "Connected to Pill Dispenser successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    gatt.requestMtu(512)
//                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("Device info", "Disconnecting bluetooth device...")
                    gatt.disconnect()
                    gatt.close()
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                Log.i("Pill Dispenser", "onMtuChanged invoked")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("MTU Request", "MTU request success")
                    gatt.discoverServices()
                } else {
                    Log.i("MTU Request", "MTU request failed")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                Log.i("Pill Dispenser", "onCharacteristicChanged invoked")
                // Read the updated characteristic value
                val message = characteristic.value
                val messageString = String(message, StandardCharsets.UTF_8)
                Log.i("Unparsed JSON string: ", messageString)
                var status = ""
                var lastDetected = 0
                val motionDetected: Boolean
                val proximityDetected: Boolean
                val lightDetected: Boolean
                val vibrationDetected: Boolean
                try {
//                    val jsonObject = JSONObject(messageString)
//                    status = jsonObject.getString("status")
//                    lastDetected = jsonObject.getInt("lastDetected")
//                    motionDetected = jsonObject.getBoolean("motion")
//                    proximityDetected = jsonObject.getBoolean("proximity")
//                    lightDetected = jsonObject.getBoolean("light")
//                    vibrationDetected = jsonObject.getBoolean("vibration")
//                    //float lightIntensity = (float) jsonObject.getDouble("lightIntensity");
//                    val finalStatus = status
//                    val finalLastDetected = lastDetected
                    runOnUiThread {
//                        textView!!.text =
//                            "Status: $finalStatus\nMotion: $motionDetected\nProximity: $proximityDetected\nLight: $lightDetected\nVibration: $vibrationDetected"
//                        lastDetection!!.text = "Last detected: " + finalLastDetected + "m ago"
//                        displayNotification!!.text = "Status: $finalStatus\nMotion: $motionDetected\nProximity: $proximityDetected\nLight: $lightDetected\nVibration: $vibrationDetected"
//                        displayNotification!!.text = messageString
                    }
                } catch (e: JSONException) {
                    Log.i("Error", "Could not parse JSON string")
                }
                Log.i("Notification", "Updated status: $status")
                Log.i("Notification", "Last detected: $lastDetected")
                // Do something with the updated characteristic value
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                Log.i("Pill Dispenser", "onServicesDiscovered invoked")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(UUID.fromString(SERVICE_UUID))
                    deviceGatt = gatt
                    deviceService = service
//                    sendData()
                    val operation = queue.poll()
                    if(!queue.isEmpty() && operation.equals("sendData")){
                        sendData(gatt)
                    }
//                    sendData(gatt)
                    Log.i("Device info", "Successfully discovered services of target device")
                    if (service != null) {
                        Log.i("Service status", "Service is not null.")
                        val discoveredCharacteristic =
                            service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                        if (discoveredCharacteristic != null) {
                            gatt.readCharacteristic(discoveredCharacteristic)
                            if (gatt.setCharacteristicNotification(discoveredCharacteristic, true)) {
                                Log.i("Set characteristic notification", "Success!")
                                Log.i(
                                    "Characteristic property flags",
                                    discoveredCharacteristic.properties.toString()
                                )
                            } else {
                                Log.i("Set characteristic notification", "Failure!")
                            }
                        } else {
                            Log.i("Characteristic info", "Characteristic not found!")
                        }
                    } else {
                        Log.i("Service info", "Service not found!")
                    }
                } else {
                    Log.i("Service Discovery", "Service discovery failed")
                }
            }

        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, status: Int) {

                Log.i("Pill Dispenser", "onCharacteristicRead invoked")

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = discoveredCharacteristic.value
                    val value = String(data, StandardCharsets.UTF_8)
                    Log.i("Read data", "Received data: $value")
                    displayNotification!!.text = value
                }
            }

        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, value : ByteArray ,status: Int) {


            Log.i("Pill Dispenser", "onCharacteristicRead with byte array invoked")
            val value = String(value)
            Log.i("Read data", "Received data: $value")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                Log.i("Pill Dispenser", "onCharacteristicWrite invoked")
                Log.i("Pill Dispenser", "onCharacteristicWrite invoked: " + characteristic.uuid)
//                val toString = characteristic.toString()
                val data = characteristic.value
                val info = gatt.readCharacteristic(characteristic)
                info.toString()
                val value = String(data)
                Log.i("Read data", "Received data: $value")

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val data = characteristic.value
                    val value = String(data, StandardCharsets.UTF_8)
                    Log.i("Read data", "Received data: $value")

                }
            }
        }

    @SuppressLint("MissingPermission")
        val someActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            super@MainActivity.onActivityResult(
                REQUEST_ENABLE_BT,
                result.resultCode,
                result.data
            )
            if (result.resultCode == RESULT_OK) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.BLUETOOTH_SCAN),
                        REQUEST_ENABLE_BT
                    )
                } else {
                    val data = result.data
                    adapter.enable()
                }
            }
        }

        fun initializeAdapters() {
            if (adapter == null) {
                Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show()
                return
            }
            if (adapter.isEnabled) {
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show()
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                someActivityResultLauncher.launch(enableBtIntent)
            }
            scanForBluetooth!!.visibility = View.VISIBLE
        }

        open fun scanForBluetooth() {
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showExplanation("Permission Needed","Rationale",Manifest.permission.BLUETOOTH_SCAN,REQUEST_BLUETOOTH_SCAN)
                } else {
                    requestPermissions( arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_SCAN )
                }
            } else {
                bluetoothScanner.startScan(scanCallback)
                startBtn!!.visibility = View.VISIBLE
            }
        }

    private fun showExplanation(s: String, s1: String, bluetoothScan: String, requestBluetoothScan: Int) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(s)
            setMessage(s1)
            setPositiveButton("OK"
            ) { dialog, which -> requestPermissions( arrayOf<String>(bluetoothScan), requestBluetoothScan )
            }.show()
        }
    }

    @SuppressLint("MissingPermission")
        open fun startProcess() {
            var device: BluetoothDevice? = null
            var targetDeviceAddress = ""
            val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
            val scannedDevicesList = java.util.ArrayList<BluetoothDevice>()
            var notFound = true
            while (connectedDevices.isEmpty() && notFound) {
                if (!devices.isEmpty()) {
                    val deviceSize = devices.size
                    for (i in 0 until deviceSize) {
                        if (devices[i] != null && devices[i].name != null) {
                            targetDeviceAddress = devices[i].name
                            device = devices[i]
                            Log.i("Device Found", "Found target device: " + device.name)
                            Log.i("Device Address", "Device address is: $targetDeviceAddress")
                            scannedDevicesList.add(devices[i])
                            bluetoothScanner.stopScan(scanCallback)
                            notFound = false
//                            break
                            /*if (devices.get(i).getName().equals("ESP32")) {
                            targetDeviceAddress = devices.get(i).getName();
                            device = devices.get(i);
                            Log.i("Device Found", "Found target device: " + device.getName());
                            Log.i("Device Address", "Device address is: " + targetDeviceAddress);
                            scannedDevicesList.add(devices.get(i));
                            bluetoothScanner.stopScan(scanCallback);
                            notFound = false;
                            break;
                        }*/
                        }
                    }
                    if (device == null) {
                        Log.i("Devices", "Target device was not found")
                        return
                    }
                } else {
                    Log.i("Devices", "No devices were found")
                    return
                }
                createButtons(scannedDevicesList)
                //            BluetoothGatt gatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
//            connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT);
            }
            initializeBluetooth!!.visibility = View.INVISIBLE
            scanForBluetooth!!.visibility = View.INVISIBLE
            startBtn!!.visibility = View.INVISIBLE
        }

        @SuppressLint("MissingPermission")
         fun createButtons(scannedDevicesList: java.util.ArrayList<BluetoothDevice>) {

            val removeDups = java.util.ArrayList<BluetoothDevice>()
            val duplicates = hashSetOf<String>()

            for (device in scannedDevicesList) {
                if (!duplicates.contains(device.name)) {
                    removeDups.add(device)
                    duplicates.add(device.name)
                }
            }

            for (device in removeDups) {
                val button = Button(this)
                button.text = device.name
                button.setOnClickListener { startConnection(device) }
                buttonContainer!!.addView(button)
            }

        }

        @SuppressLint("MissingPermission")
        fun startConnection(device: BluetoothDevice) {
            if(this.device == null){
                this.device = device
            }
            val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            var connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
            val gatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)

//            startDeviceDiscovery("sendData")
            Log.i("Bluetooth Device Check", device.toString())


        }

        @SuppressLint("MissingPermission")
        override fun autoConnect(){
            var newDevice =  this.device
    //                val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    //                val gatt = device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            Log.d("AlarmReceiver", "Repeating alarm triggered from autoConnect!")
        }

        @SuppressLint("MissingPermission")
        fun startDeviceDiscovery(operation: String){
            val gatt = this.device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            queue.add(operation)
            gatt?.discoverServices()
//            sendDataBtn.visibility = View.VISIBLE
        }

        @SuppressLint("MissingPermission")
        fun sendData(gatt: BluetoothGatt){

            queue.add("sendData")
            val s = "ok"
            val charsetName = "UTF-16"
            val byteArray = s.toByteArray(StandardCharsets.UTF_8)

            deviceService = gatt?.getService(UUID.fromString(SERVICE_UUID))

            if (deviceService != null) {
                val example: BluetoothGattCharacteristic = deviceService!!.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

                if (example != null) {
                    Log.i("Permission Value", example.permissions.toString() + "")
                    example.value = byteArray
                    gatt?.writeCharacteristic(example)

                    Log.i("Send Data", "the data was sent!")
                }
            }

        }

        @SuppressLint("MissingPermission")
        fun sendDataCallback(){

            Log.i("Send Data", "callback function was triggered")
            val s = "ok"
            val charsetName = "UTF-16"
            val byteArray = s.toByteArray(StandardCharsets.UTF_8)

            deviceService = deviceGatt?.getService(UUID.fromString(SERVICE_UUID))

            if (deviceService != null) {
                val example: BluetoothGattCharacteristic = deviceService!!.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                //                val example: BluetoothGattCharacteristic = BluetoothGattCharacteristic(UUID.fromString(CHARACTERISTIC_UUID), 8, 16)

                if (example != null) {
                    Log.i("Permission Value", example.permissions.toString() + "")
                    example.value = byteArray
                    deviceGatt?.writeCharacteristic(example)

                    //                    Below are write characteristics for API 33
                    //                    gatt?.writeCharacteristic(example, byteArray, 2)
                    //                    device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                    //                        ?.writeCharacteristic(example, byteArray, 2)
                    //                    gatt.writeCharacteristic(example)

                    Log.i("Send Data", "the data was sent!")
                }
            }

        }

        @SuppressLint("MissingPermission")
        fun sendToDevice(message: String){

            Log.i("Send Data", "callback function was triggered")
            val s = message
            val charsetName = "UTF-16"
            val byteArray = s.toByteArray(StandardCharsets.UTF_8)

            deviceService = deviceGatt?.getService(UUID.fromString(SERVICE_UUID))

            if (deviceService != null) {
                val example: BluetoothGattCharacteristic = deviceService!!.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                //                val example: BluetoothGattCharacteristic = BluetoothGattCharacteristic(UUID.fromString(CHARACTERISTIC_UUID), 8, 16)

                if (example != null) {
                    Log.i("Permission Value", example.permissions.toString() + "")
                    example.value = byteArray
                    deviceGatt?.writeCharacteristic(example)

                    //                    Below are write characteristics for API 33
                    //                    gatt?.writeCharacteristic(example, byteArray, 2)
                    //                    device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                    //                        ?.writeCharacteristic(example, byteArray, 2)
                    //                    gatt.writeCharacteristic(example)

                    Log.i("Send Data", "the data was sent!")
                }
            }

        }

        open fun switchLayouts(layout: String) {
            if (layout == "setTime") {
//            setContentView(R.layout.set_date);
                timeButton!!.visibility = View.VISIBLE
                initializeBluetooth!!.visibility = View.INVISIBLE
                scanForBluetooth!!.visibility = View.INVISIBLE
                startBtn!!.visibility = View.INVISIBLE
                buttonContainer!!.visibility = View.INVISIBLE
                textView!!.visibility = View.INVISIBLE
                lastDetection!!.visibility = View.INVISIBLE
                timeButton.setOnClickListener { setTime(timeButton) }
            } else {
                setContentView(R.layout.activity_main)
            }
        }

        fun setTime(timeButton: Button) {
            val onTimeSetListener = OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                    timeButton.text =
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                }
            val style = android.R.style.Theme_Holo_Light_Dialog_NoActionBar
            val timePickerDialog =
                TimePickerDialog(this, style, onTimeSetListener, hour, minute, true)
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }

        fun setTime(day: String) {
            val onTimeSetListener = OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                schedule[day] = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                Log.i("Schedule", schedule.toString())
                sendToDevice(schedule.toString())
//                timeButton.text =
//                    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            }
            val style = android.R.style.Theme_Holo_Light_Dialog_NoActionBar
            val timePickerDialog =
                TimePickerDialog(this, style, onTimeSetListener, hour, minute, true)
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }

        fun switchToNewActivity(){
            val intent = Intent(this, PillDispenserActivity::class.java)
            startActivity(intent)
        }

        private fun launchBackgroundScan() {
//            startBackgroundScan = Intent(this, BackgroundScan::class.java)
            startBackgroundScan = android.content.Intent(this, BackgroundScan::class.java)
            startBackgroundScan!!.putExtra("service_uuid", SERVICE_UUID)
            startBackgroundScan!!.putExtra("characteristic_uuid", CHARACTERISTIC_UUID)
            startService(startBackgroundScan)
            bindService(startBackgroundScan, mConnection, BIND_AUTO_CREATE)
//            devicesList.setVisibility(View.VISIBLE)
//            stopBackgroundScanBtn.setVisibility(View.VISIBLE)
        }

        override fun onDeviceScan(result: android.bluetooth.le.ScanResult?) {
            var device: BluetoothDevice? = result?.getDevice()
            var uuids: kotlin.collections.MutableList<ParcelUuid?>? =
                result?.getScanRecord()?.getServiceUuids()
            addDeviceToList(device, uuids)
        }


        override fun onTargetDeviceFound(device: BluetoothDevice?) {
            Log.i("BACKGROUND SERVICE", "onTargetDeviceFound running")
            val gatt = device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//                       device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        }

        open fun addDeviceToList(device: BluetoothDevice?, uuids: kotlin.collections.MutableList<ParcelUuid?>?) {
            if (((device != null) && (device.getName() != null) && !device.getName().isEmpty())) {
                if (!serviceScannedDevices.contains(MyBluetoothDevice(device, uuids))) {
                    if ((device.getName() == "ESP32")) {
                        serviceScannedDevices?.add(0, MyBluetoothDevice(device, uuids))
                    } else {
                        serviceScannedDevices?.add(MyBluetoothDevice(device, uuids))
                    }
                    if (deviceNameMapping.containsKey(device.getName())) {
                        deviceNameMapping?.get(device.getName())?.add(MyBluetoothDevice(device, uuids))
                    } else {
                        val devicesList: MutableList<MyBluetoothDevice> = mutableListOf()
                        devicesList?.add(MyBluetoothDevice(device, uuids))
                        deviceNameMapping?.put(device.getName().uppercase(java.util.Locale.getDefault()), devicesList)
                    }
//                    adapter.updateDataset(serviceScannedDevices)

                }
            }
        }

        lateinit var tabLayout: TabLayout
        lateinit var viewPager2: ViewPager2
        lateinit var myViewPagerAdapter: MyViewPagerAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            tabLayout = findViewById(R.id.tab_layout)
            viewPager2 = findViewById(R.id.view_pager)
            myViewPagerAdapter = MyViewPagerAdapter(this)
//            myViewPagerAdapter = MyViewPagerAdapter(supportFragmentManager, lifecycle)
            viewPager2.adapter = myViewPagerAdapter
            context = this

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab : TabLayout.Tab){
                    viewPager2.currentItem = tab.position
                    initializeBluetooth = findViewById<Button>(R.id.initializeBluetooth)
                    scanForBluetooth = findViewById<Button>(R.id.scanBluetooth)
                    sendDataBtn = findViewById(R.id.sendData)

                    startBtn = findViewById(R.id.startBtn)
                    buttonContainer = findViewById<LinearLayout>(R.id.scannedDevices)
                    scanForBluetooth.visibility = View.INVISIBLE
                    startBtn.visibility = View.INVISIBLE
                    displayNotification = findViewById<TextView>(R.id.showNotification)
                    startServiceBtn = findViewById<Button>(R.id.startServiceBtn)

                    initializeBluetooth.setOnClickListener(View.OnClickListener { initializeAdapters() })
                    sendDataBtn.setOnClickListener( View.OnClickListener { sendDataCallback() } )

                    setSunday = findViewById<Button>(R.id.Sunday)
                    setSunday.setOnClickListener(View.OnClickListener { setTime("Sunday") })
                    setMonday = findViewById<Button>(R.id.Monday)
                    setMonday.setOnClickListener(View.OnClickListener { setTime("Monday") })
                    setTuesday = findViewById<Button>(R.id.Tuesday)
                    setTuesday.setOnClickListener(View.OnClickListener { setTime("Tuesday") })
                    setWednesday = findViewById<Button>(R.id.Wednesday)
                    setWednesday.setOnClickListener(View.OnClickListener { setTime("Wednesday") })
                    setThursday = findViewById<Button>(R.id.Thursday)
                    setThursday.setOnClickListener(View.OnClickListener { setTime("Thursday") })
                    setFriday = findViewById<Button>(R.id.Friday)
                    setFriday.setOnClickListener(View.OnClickListener { setTime("Friday") })
                    setSaturday = findViewById<Button>(R.id.Saturday)
                    setSaturday.setOnClickListener(View.OnClickListener { setTime("Saturday") })

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        scanForBluetooth.setOnClickListener(View.OnClickListener { scanForBluetoothWithPermissions() })
                    }else {
                        scanForBluetooth.setOnClickListener(View.OnClickListener { scanForBluetooth() })
                    }

                    startBtn.setOnClickListener(View.OnClickListener { startProcess() })
                    startServiceBtn.setOnClickListener(View.OnClickListener { scanForBluetoothWithPermissionsService() })

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout.getTabAt(position)?.select()
                }

            })

        }

        @SuppressLint("MissingPermission")
        fun scanForBluetoothWithPermissions(){
            if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
                showAlert()
            } else {
                Toast.makeText(this, "Permissions already granted.", Toast.LENGTH_SHORT).show()
                Log.i("Permission", "Scan Permission granted")
                bluetoothScanner.startScan(scanCallback)
                startBtn!!.visibility = View.VISIBLE
            }
        }

        private fun isPermissionsGranted(): Int {
            var counter = 0;
            for (permission in list) {
                counter += ContextCompat.checkSelfPermission(this, permission)
            }
            return counter
        }

        private fun showAlert() {
            Log.i("Permission", "showAlert function triggered")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Need permission(s)")
            builder.setMessage("Bluetooth permissions are required to do the task.")
            builder.setPositiveButton("OK", { dialog, which -> requestPermissions() })
            builder.setNeutralButton("Cancel", null)
            val dialog = builder.create()
            dialog.show()
        }

        private fun requestPermissions() {
            val permission = deniedPermission()
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show an explanation asynchronously
                Toast.makeText(this, "Should show an explanation.", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, list.toTypedArray(), permissionrequestcode)
            }
        }

        private fun deniedPermission(): String {
            for (permission in list) {
                if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_DENIED) return permission
            }
            return ""
        }

        fun processPermissionsResult(requestCode: Int, permissions: Array<String>,
                                     grantResults: IntArray): Boolean {
            var result = 0
            if (grantResults.isNotEmpty()) {
                for (item in grantResults) {
                    result += item
                }
            }
            if (result == PackageManager.PERMISSION_GRANTED) return true
            return false
        }

        @SuppressLint("MissingPermission")
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            when (requestCode) {
                permissionrequestcode -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permissions already granted.", Toast.LENGTH_SHORT).show()
                        Log.i("Permission", "Scan Permission granted")
                        bluetoothScanner.startScan(scanCallback)
                        launchBackgroundScan()
                        startBtn!!.visibility = View.VISIBLE
                    } else {
                        showAlert()
                    }
                }
            }
        }

    // Services

    @SuppressLint("MissingPermission")
    fun scanForBluetoothWithPermissionsService(){
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlertService()
        } else {
            Toast.makeText(this, "Permissions already granted.", Toast.LENGTH_SHORT).show()
            Log.i("Permission", "Scan Permission granted")
            launchBackgroundScan()
        }
    }

    private fun showAlertService() {
        Log.i("Permission", "showAlert function triggered")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Bluetooth permissions are required to do the task.")
        builder.setPositiveButton("OK", { dialog, which -> requestPermissions() })
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

}