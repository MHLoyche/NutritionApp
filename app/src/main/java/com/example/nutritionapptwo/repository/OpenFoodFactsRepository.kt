package com.example.nutritionapptwo.repository

import android.util.Log
import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// My data layer that fetches product data from OpenFoodFacts API based on barcode
// and converts the api response into a ScannedItem object
// this is the middle man between the ViewModel and the network layer

class OpenFoodFactsRepository {

    // Tag for logging
    companion object {
        private const val TAG = "OpenFoodFactsRepository"
    }

    // Fetches product data for the given barcode and maps it to a ScannedItem
    // Using suspend function to perform network operation off the main thread (withContext(Dispatchers.IO))
    suspend fun getItemForBarcode(barcode: String): ScannedItem? = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getProduct(barcode) // get product data from API

            if (response.status != 1 || response.product == null) {
                Log.w(TAG, "Product not found for barcode=\$barcode, status=\${response.status}")
                return@withContext null
            }

            val p = response.product // using product dto from response
            val n = p.nutriments // using nutriments dto from product

            val carbs100g = (n?.carbohydrates_100g ?: 0.0)
            val protein100g = (n?.proteins_100g ?: 0.0)
            val fat100g = (n?.fat_100g ?: 0.0)
            val kcalFromApi = (n?.energy_kcal_100g ?: 0.0)

            val kcalComputed = if (kcalFromApi > 0.0) {
                kcalFromApi
            } else {
                4.0 * carbs100g + 4.0 * protein100g + 9.0 * fat100g // Fallback calculation if kcal is missing
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
