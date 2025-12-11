package com.example.nutritionapptwo.repository

import android.util.Log
import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenFoodFactsRepository {

    companion object {
        private const val TAG = "OpenFoodFactsRepository"
    }

    suspend fun getItemForBarcode(barcode: String): ScannedItem? = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getProduct(barcode)

            if (response.status != 1 || response.product == null) {
                Log.w(TAG, "Product not found for barcode=\$barcode, status=\${response.status}")
                return@withContext null
            }

            val p = response.product
            val n = p.nutriments

            val carbs100g = (n?.carbohydrates_100g ?: 0.0)
            val protein100g = (n?.proteins_100g ?: 0.0)
            val fat100g = (n?.fat_100g ?: 0.0)
            val kcalFromApi = (n?.energy_kcal_100g ?: 0.0)

            val kcalComputed = if (kcalFromApi > 0.0) {
                kcalFromApi
            } else {
                4.0 * carbs100g + 4.0 * protein100g + 9.0 * fat100g
            }

            ScannedItem(
                barcode = barcode,
                name = p.product_name ?: "unknown product",
                calories = kcalComputed.toInt(),
                protein = protein100g.toInt(),
                fat = fat100g.toInt(),
                carbohydrates = carbs100g.toInt()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product for barcode=\$barcode", e)
            null
        }
    }
}
