package com.example.nutritionapptwo.network

data class OpenFoodFactsResponse(
    val status: Int? = null,
    val product: ProductDto? = null
)

data class ProductDto(
    val code: String? = null,
    val product_name: String? = null,
    val nutriments: NutrimentsDto? = null
)

data class NutrimentsDto(
    val energy_kcal_100g: Int? = null,
    val protein_100g: Int? = null,
    val fat_100g: Int? = null,
    val carbohydrates_100g: Int? = null
)
