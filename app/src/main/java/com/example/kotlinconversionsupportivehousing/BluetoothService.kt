package com.example.kotlinconversionsupportivehousing

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.nio.charset.StandardCharsets
import java.util.LinkedList
import java.util.Queue
import java.util.UUID


class BluetoothService : Service() {
    var devices = ArrayList<BluetoothDevice>()
    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val bluetoothScanner = adapter.bluetoothLeScanner
    var device: BluetoothDevice? = null
    var deviceService: BluetoothGattService? = null
    var deviceGatt: BluetoothGatt? = null
    val queue: Queue<String> = LinkedList()
    val handlerLoop = Handler(Looper.getMainLooper())


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BluetoothService","Service on create method")
        // Initialization code here
    }


    @SuppressLint("MissingPermission")
    val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("Pill Dispenser", "onConnectionStateChange invoked")
            Log.i("Device Status", newState.toString() + "")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Device info", "Discovering bluetooth services of target device...")
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(
                        this@BluetoothService,
                        "Connected to Pill Dispenser successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                gatt.requestMtu(512)
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
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            Log.i("Pill Dispenser", "onCharacteristicChanged invoked")
            val message = characteristic.value
            val messageString = String(message, StandardCharsets.UTF_8)
            Log.i("Unparsed JSON string: ", messageString)
            var status = ""
            var lastDetected = 0
            val motionDetected: Boolean
            val proximityDetected: Boolean
            val lightDetected: Boolean
            val vibrationDetected: Boolean
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
                val operation = queue.poll()
                if (!queue.isEmpty() && operation.equals("sendData")) {
                    sendData(gatt)
                }
                Log.i("Device info", "Successfully discovered services of target device")
                if (service != null) {
                    Log.i("Service status", "Service is not null.")
                    val discoveredCharacteristic =
                        service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                    if (discoveredCharacteristic != null) {
                        gatt.readCharacteristic(discoveredCharacteristic)
                        if (gatt.setCharacteristicNotification(
                                discoveredCharacteristic,
                                true
                            )
                        ) {
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

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            discoveredCharacteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

            Log.i("Pill Dispenser", "onCharacteristicRead invoked")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")

            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            discoveredCharacteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Log.i("Pill Dispenser", "onCharacteristicRead with byte array invoked")
            val value = String(value)
            Log.i("Read data", "Received data: $value")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.i("Pill Dispenser", "onCharacteristicWrite invoked")
            Log.i("Pill Dispenser", "onCharacteristicWrite invoked: " + characteristic.uuid)
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
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service is starting, handle intent here
        var value = intent?.getStringExtra("device")
        var normalvalue: String? = null
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                if (extras.containsKey("device")) {
                    val obj = extras["device"]
                    if (obj is String) {
                        value = obj
                    } else {
                        // Handle the case where obj is not a String
                        // For example, log an error or throw an exception
                        Log.e(
                            "IntentError",
                            "Expected a String for 'device', but found: " + obj!!.javaClass.simpleName
                        )
                    }
                }
            }
        }
        if (normalvalue != null) {
            Log.d("Bluetooth Service",normalvalue)
        }
        Log.d("BluetoothService","Service of Bluetooth")
        val runnable = Runnable {
            if(this.device == null){
                this.device = device
            }
            Log.d("BluetoothService","startcommand call  Connected")
            val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            var connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
            val gatt = device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

        }
        handlerLoop.postDelayed(runnable, 5000)


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerLoop.removeCallbacksAndMessages(null)
        // Cleanup code here
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

}