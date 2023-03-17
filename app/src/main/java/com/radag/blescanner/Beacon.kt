package com.radag.blescanner

class Beacon() {

    var macAddress: String? = null
    var deviceName: String? = null
    var rssi: Int? = null
    var distance: Double? = null

//trngo que revisar como solucionar esto, este codigo hace que si hay otra beacon con el mismo mac
//no la agregue, lo cual me fuerza a borrarla y agregarla cada vez que se actualiza el escaneo
//yo lo unico que quiero entonces es cambiar los datos no borrarlos


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