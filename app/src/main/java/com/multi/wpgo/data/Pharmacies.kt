package com.multi.wpgo.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Pharmacies (
    @SerializedName("pharmacies") val pharmacies: List<Pharmacy>
) : Serializable