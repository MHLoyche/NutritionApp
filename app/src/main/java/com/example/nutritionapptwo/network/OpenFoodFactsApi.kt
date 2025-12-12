package com.example.nutritionapptwo.network

import retrofit2.http.GET
import retrofit2.http.Path

// Retrofit interface for OpenFoodFacts API - Defines endpoint to fetch product data by barcode
interface OpenFoodFactsApi {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}