package com.example.nutritionapptwo.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Use of Retrofit to interact with the OpenFoodFacts API - Fetching product data based on barcode

object ApiClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}