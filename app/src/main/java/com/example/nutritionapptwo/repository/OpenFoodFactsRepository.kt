package com.example.nutritionapptwo.repository

import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenFoodFactsRepository {

    suspend fun getItemForBarcode(barcode: String): ScannedItem? = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getProduct(barcode)
            if (response.status == 1 || response.product == null) {
                return@withContext null
            }

            val p = response.product
            val n = p.nutriments

            ScannedItem(
                barcode = barcode,
                name = p.product_name ?: "unknown product",
                calories = n?.energy_kcal_100g?.toInt() ?: 0,
                protein = n?.protein_100g?.toInt() ?: 0,
                fat = n?.fat_100g?.toInt() ?: 0,
                carbohydrates = n?.carbohydrates_100g?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }
}