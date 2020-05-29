package com.multi.wpgo.data.network

import com.multi.wpgo.data.Pharmacies
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface RetrofitService {

    @GET("pharmacies")
    suspend fun getPharmacies(): Pharmacies

}