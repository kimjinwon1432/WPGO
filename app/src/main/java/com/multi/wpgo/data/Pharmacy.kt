package com.multi.wpgo.data

import java.io.Serializable

data class Pharmacy (
    var id: Int = 0,
    var name: String = "",
    var addr: String = "",
    var tel: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var opWeekday: String = "",
    var opSat: String = "",
    var opSun: String = "",
    var opHoliday: String = "",
    var isOpNow: Boolean = false
) : Serializable