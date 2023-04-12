package com.radag.blescanner

class Beacon() {

    var macAddress: String? = null
    var deviceName: String? = null
    var rssi: Int? = null
    var rssiAverage: Int? = null
    var distance: Double? = null
    var major: Int? = null
    var minor: Int? = null
    var lista = mutableListOf<Int>()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Beacon) return false
        if (macAddress != other.macAddress) return false
        return true
    }

    override fun hashCode(): Int {
        return macAddress?.hashCode() ?: 0
    }
}