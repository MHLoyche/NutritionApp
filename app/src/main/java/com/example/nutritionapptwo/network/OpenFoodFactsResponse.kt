package com.example.nutritionapptwo.network

import com.google.gson.annotations.SerializedName

// Data classes representing the structure of the OpenFoodFacts API response
// Turns raw web response into usable objects in the app

data class OpenFoodFactsResponse(
    @SerializedName("status")
    val status: Int = 0,
    @SerializedName("product")
    val product: ProductDto? = null
)

data class ProductDto(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("product_name")
    val product_name: String? = null,
    @SerializedName("nutriments")
    val nutriments: NutrimentsDto? = null
)

data class NutrimentsDto(
    @SerializedName("energy-kcal_100g")
    val energy_kcal_100g: Double? = null,
    @SerializedName("proteins_100g")
    val proteins_100g: Double? = null,
    @SerializedName("fat_100g")
    val fat_100g: Double? = null,
    @SerializedName("carbohydrates_100g")
    val carbohydrates_100g: Double? = null
)