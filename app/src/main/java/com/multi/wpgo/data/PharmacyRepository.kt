package com.multi.wpgo.data

import com.multi.wpgo.data.network.RetroClient
import com.multi.wpgo.data.network.RetrofitService

class PharmacyRepository {

    var client: RetrofitService = RetroClient.webservice

    suspend fun getPharmacies() = client.getPharmacies()

}