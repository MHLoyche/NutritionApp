package com.example.nutritionapptwo.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritionapptwo.model.MealDetails
import com.example.nutritionapptwo.model.ScannedItem
import com.example.nutritionapptwo.repository.OpenFoodFactsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealViewModel : ViewModel() {
    private val repository: OpenFoodFactsRepository = OpenFoodFactsRepository()
    private val _selectedDate = MutableStateFlow(Date().toLocalDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Primary in-memory storage: mutable map of date -> (mealName -> list of items)
    private val _mealsByDate = mutableStateMapOf<String, MutableMap<String, MutableList<ScannedItem>>>()

    // Exposed observable StateFlows (derived state) for the currently selected date
    private val _itemsByMeal = MutableStateFlow<Map<String, List<ScannedItem>>>(emptyMap())
    val itemsByMeal: StateFlow<Map<String, List<ScannedItem>>> = _itemsByMeal.asStateFlow()

    private val _mealNutrientsByMeal = MutableStateFlow<Map<String, MealDetails>>(emptyMap())
    val mealNutrientsByMeal: StateFlow<Map<String, MealDetails>> = _mealNutrientsByMeal.asStateFlow()

    private val _totalForSelectedDate = MutableStateFlow(MealDetails(0, 0, 0, 0))
    val totalForSelectedDate: StateFlow<MealDetails> = _totalForSelectedDate.asStateFlow()

    init {
        // Ensure derived StateFlows are initialized from the empty storage
        refreshDerivedState()
    }

    fun setSelectedDate(date: String){
        _selectedDate.value = date
        refreshDerivedState()
    }

    /** Returns the current items for a meal (read-only snapshot). Prefer collecting [itemsByMeal] for updates. */
    fun getItemsForMeal(mealName: String): List<ScannedItem> {
        return _itemsByMeal.value[mealName] ?: emptyList()
    }

    fun addItemToMeal(mealName: String, item: ScannedItem) {
        val date = _selectedDate.value
        val mealsForDate = _mealsByDate.getOrPut(date) { mutableMapOf() }
        val itemsForMeal = mealsForDate.getOrPut(mealName) { mutableListOf() }
        itemsForMeal.add(item)
        // Update derived observable state so UIs observing flows update immediately
        refreshDerivedState()
    }

    fun removeItemFromMeal(mealName: String, item: ScannedItem) {
        val date = _selectedDate.value
        val mealsForDate = _mealsByDate[date] ?: return
        val itemsForMeal = mealsForDate[mealName] ?: return
        itemsForMeal.remove(item)
        refreshDerivedState()
    }

    fun updateItemServingSize(mealName: String, item: ScannedItem, newGrams: Int) {
        val date = _selectedDate.value
        val mealsForDate = _mealsByDate[date] ?: return
        val itemsForMeal = mealsForDate[mealName] ?: return
        val index = itemsForMeal.indexOf(item)
        if (index >= 0) {
            // Replace the item with adjusted nutrients
            itemsForMeal[index] = item.withServingSize(newGrams)
            refreshDerivedState()
        }
    }

    /** Returns a snapshot of nutrients for the given meal. Prefer collecting [mealNutrientsByMeal] for updates. */
    fun getNutrientsForMeal(mealName: String): MealDetails {
        return _mealNutrientsByMeal.value[mealName] ?: MealDetails(0, 0, 0, 0)
    }

    /** Returns a snapshot of the total nutrients for the selected date. Prefer collecting [totalForSelectedDate] for updates. */
    fun getTotalNutrients(): MealDetails = _totalForSelectedDate.value

    // Recompute the derived StateFlows for the currently selected date from the underlying storage
    private fun refreshDerivedState() {
        val date = _selectedDate.value
        val mealsForDate = _mealsByDate[date] ?: emptyMap()

        // itemsByMeal: immutable lists
        val itemsMap: Map<String, List<ScannedItem>> = mealsForDate.mapValues { it.value.toList() }
        _itemsByMeal.value = itemsMap

        // per-meal nutrients
        val nutrientsMap: Map<String, MealDetails> = itemsMap.mapValues { (_, list) ->
            MealDetails(
                kcal = list.sumOf { it.calories },
                protein = list.sumOf { it.protein },
                carbs = list.sumOf { it.carbohydrates },
                fat = list.sumOf { it.fat }
            )
        }
        _mealNutrientsByMeal.value = nutrientsMap

        // total nutrients for the date
        val total = nutrientsMap.values.fold(MealDetails(0, 0, 0, 0)) { acc, m ->
            MealDetails(
                kcal = acc.kcal + m.kcal,
                protein = acc.protein + m.protein,
                carbs = acc.carbs + m.carbs,
                fat = acc.fat + m.fat
            )
        }
        _totalForSelectedDate.value = total
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

    private fun Date.toLocalDateString(): String {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return format.format(this)
    }
}
