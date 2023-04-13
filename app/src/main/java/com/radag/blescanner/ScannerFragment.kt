package com.radag.blescanner

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.pow
import android.speech.tts.TextToSpeech


class ScannerFragment : Fragment() {

    private lateinit var distancia: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var textToSpeech: TextToSpeech

    private var handler: Handler? = null
    private var btManager: BluetoothManager? = null
    private var btAdapter: BluetoothAdapter? = null
    private var btScanner: BluetoothLeScanner? = null

    var beaconSet: HashSet<Beacon> = HashSet() //inicializo un set para guardar los resultados
    var beaconAdapter: BeaconsAdapter? = null

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scanner, container, false)
        initViews(view)
        setUpBluetoothManager()

        return view
    }

    //Configuracion del Bluetooth
    private fun setUpBluetoothManager() {
        btManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager!!.adapter
        btScanner = btAdapter?.bluetoothLeScanner

        if (btAdapter != null && !btAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
        checkForLocationPermission()
    }

    //Verificacion de permisios de ubicacion
    private fun checkForLocationPermission() {
        // Verifica que la app tenga acceso a la ubicacion, si no se solicita al usuario para habilitarla
        if (activity!!.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect  peripherals.")
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_COARSE_LOCATION)
            }
            builder.show()
        }
    }

    //Resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>, grantResults: IntArray ) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover BLE beacons")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        beaconAdapter = BeaconsAdapter(beaconSet.toList())
        recyclerView.adapter = beaconAdapter
        distancia = view.findViewById(R.id.distancia)
    }


    override fun onStart() {
        super.onStart()
        val scanFilters = bleFilters()
        val scanSettings = bleSettings()
        btScanner!!.startScan (scanFilters,scanSettings,leScanCallback)

        // Start the handler to execute the text-to-speech function every 10 seconds
        handler = Handler()
        handler?.postDelayed(textToSpeechRunnable, 10000)

        textToSpeech = TextToSpeech(activity) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale("pt", "BR")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        btScanner!!.stopScan(leScanCallback)
        handler?.removeCallbacks(textToSpeechRunnable)
        handler = null
    }

    private val textToSpeechRunnable = object : Runnable {
        override fun run() {
            // Execute the text-to-speech function
            speakText()
            // Post the same runnable after 10 seconds
            handler?.postDelayed(this, 10000)
        }
    }

    private fun speakText() {
        textToSpeech.speak(distancia.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    //Configura los filtros de busqueda de dispositivos
    private fun bleFilters(): MutableList<ScanFilter> {
        val deviceName = "ESP32_BEACON"
        val scanFilters = mutableListOf<ScanFilter>()
        val scanFilter = ScanFilter.Builder().setDeviceName(deviceName).build()
        scanFilters.add(scanFilter)
        return scanFilters
    }
    //configura el escaneo
    private fun bleSettings(): ScanSettings? {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // set the scan mode (low latency, balanced, or low power)
            .setReportDelay(0) // set the delay before reporting the scan result
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT) // set the number of matches required for filtering
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE) // set the match mode (stick to one or allow multiple)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // set the callback type (direct or aggregated)
            .build()
    }

    private val rssiList = mutableListOf<Int>()

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scanRecord = result.scanRecord
            val beacon = Beacon()

            if (scanRecord != null){
                beaconSet.clear()
                beacon.macAddress=result.device.address
                beacon.deviceName= result.device.name
                beacon.rssi = result.rssi
                rssiList.add(result.rssi)
                val maxListSize = 10

                if (rssiList.size > maxListSize) {
                    rssiList.removeAt(0)
                }

                beacon.lista=rssiList
                val rssiAverage = rssiList.average().toInt()
                beacon.rssiAverage = rssiAverage
                beacon.distance= distanceCalculator(rssiAverage)

                if(result.device != null) {
                    beaconSet.add(beacon)
                    distancia.text = String.format(getString(R.string.distancia),beacon.distance)
                }

                else {
                    beaconSet.clear()
                    distancia.text = String.format(getString(R.string.noEncontrada))
                }

                (recyclerView.adapter as BeaconsAdapter).updateData(beaconSet.toList())

            }
        }

        //que hacer si el escaneo fallo
        override fun onScanFailed(errorCode: Int) {
            Log.e("radagast", errorCode.toString())
        }
    }

    //calcula la  distancia basado en el RSSI
    fun distanceCalculator(rssi: Int): Double? {
        val eta = 2.0
        val rssiOneMeter = -62.0
        val exponent: Double = (rssiOneMeter - rssi.toDouble()) / (10 * eta)

        return 10.toDouble().pow(exponent)
    }
}

