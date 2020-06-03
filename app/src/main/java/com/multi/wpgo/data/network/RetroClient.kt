package com.multi.wpgo.data.network

import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetroClient {

    private const val TIMEOUT = 5L

    private val interceptor = Interceptor { chain ->
        val newUrl = chain.request().url
            .newBuilder()
            .build()

        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()

        chain.proceed(newRequest)
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.apply {
//            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        }
    }

    private val client = OkHttpClient().newBuilder()
        .addInterceptor(interceptor)
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    val webservice: RetrofitService by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl("http://220.69.241.86/wpgo/index.php/") // 도메인 주소
            .addConverterFactory(GsonConverterFactory.create()) // GSON을 사용하기 위해 ConverterFactory에 GSON 지정
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(RetrofitService::class.java)
    }

//    private fun getService(): RetrofitService = retrofit.create(RetrofitService::class.java)

//    private const val TIMEOUT = 10L
//
//    private val okHttpClient = OkHttpClient().newBuilder().apply {
//        connectTimeout(TIMEOUT, TimeUnit.SECONDS)
//        writeTimeout(TIMEOUT, TimeUnit.SECONDS)
//        readTimeout(TIMEOUT, TimeUnit.SECONDS)
//    }.build()

//    private val retrofit =
//        Retrofit.Builder()
//            .baseUrl("http://giantstar115.dothome.co.kr/wpgo/index.php/") // 도메인 주소
////            .client(okHttpClient)
//            .addConverterFactory(NullOnEmptyConverterFactory())
//            .addConverterFactory(GsonConverterFactory.create()) // GSON을 사용하기 위해 ConverterFactory에 GSON 지정
//            .build()
}