package com.example.nutritionapptwo.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritionapptwo.model.MealDetails
import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.repository.OpenFoodFactsRepository
import kotlinx.coroutines.launch

class MealViewModel(
    private val repository: OpenFoodFactsRepository = OpenFoodFactsRepository()
) : ViewModel() {

    // mealName -> list of scanned items
    private val _mealItems = mutableStateMapOf<String, MutableList<ScannedItem>>()

    val mealItems: Map<String, List<ScannedItem>> get() = _mealItems

    fun getItemsForMeal(mealName: String): List<ScannedItem> =
        _mealItems[mealName] ?: emptyList()

    fun addItemToMeal(mealName: String, item: ScannedItem) {
        val list = _mealItems.getOrPut(mealName) { mutableListOf() }
        list.add(item)
        // trigger recomposition
        _mealItems[mealName] = list.toMutableList()
    }

    fun getNutrientsForMeal(mealName: String): MealDetails {
        val items = getItemsForMeal(mealName)
        var kcal = 0
        var protein = 0
        var carbs = 0
        var fat = 0

        items.forEach { i ->
            kcal += i.calories ?: 0
            protein += i.protein ?: 0
            carbs += i.carbohydrates ?: 0
            fat += i.fat ?: 0
        }
        return MealDetails(
            kcal = kcal,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }

    fun getTotalNutrients(): MealDetails {
        var kcal = 0
        var protein = 0
        var carbs = 0
        var fat = 0

        _mealItems.values.flatten().forEach { i ->
            kcal += i.calories ?: 0
            protein += i.protein ?: 0
            carbs += i.carbohydrates ?: 0
            fat += i.fat ?: 0
        }

        return MealDetails(
            kcal = kcal,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }

    fun addItemFromBarcode(mealName: String, barcode: String) {
        viewModelScope.launch {
            val item = repository.getItemForBarcode(barcode)
            if (item != null) {
                addItemToMeal(mealName, item)
            } else {
                // Fallback if API fails: at least keep the barcode
                addItemToMeal(mealName, ScannedItem(barcode = barcode))
            }
        }
    }


}
