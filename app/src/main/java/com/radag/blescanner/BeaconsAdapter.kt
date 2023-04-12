package com.radag.blescanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BeaconsAdapter(beacons: List<Beacon>) :
    RecyclerView.Adapter<BeaconsAdapter.BeaconHolder>() {
    var beaconList: MutableList<Beacon> = beacons.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BeaconHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: BeaconHolder, position: Int) {
        val beacon: Beacon = beaconList[position]
        holder.bind(beacon)
    }

    override fun getItemCount() = beaconList.size

    fun updateData(data: List<Beacon>) {
        beaconList.clear()
        beaconList.addAll(data)
        notifyDataSetChanged()
    }

    class BeaconHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.scan_result_items, parent, false)) {
        private var image: ImageView? = null
        private var mac: TextView? = null
        private var rssi: TextView? = null
        private var beaconName: TextView? = null
        private var beaconDistance: TextView? = null
        private var rssiAverage: TextView? = null
        private var lista: TextView? = null


        private val context = parent.context

        init {
            image = itemView.findViewById(R.id.beacon_image)
            mac = itemView.findViewById(R.id.beacon_mac)
            rssi = itemView.findViewById(R.id.beacon_rssi)
            beaconName = itemView.findViewById(R.id.beacon_name)
            beaconDistance = itemView.findViewById(R.id.beacon_distance)
            rssiAverage = itemView.findViewById(R.id.beacon_rssiAverage)
            lista = itemView.findViewById(R.id.lista)



        }

        fun bind(beacon: Beacon) {
            image?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bluetooth))
            mac?.text = String.format(context.getString(R.string.mac),beacon.macAddress)
            rssi?.text = String.format(context.getString(R.string.rssi),beacon.rssi)
            beaconName?.text = String.format(context.getString(R.string.name),beacon.deviceName)
            beaconDistance?.text = String.format(context.getString(R.string.distance),beacon.distance)
            rssiAverage?.text = String.format(context.getString(R.string.rssiAverage),beacon.rssiAverage)
            lista?.text = String.format(beacon.lista.toString())


        }
    }
}


