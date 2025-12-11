package com.example.nutritionapptwo.model

data class ScannedItem(
    val barcode: String,
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbohydrates: Int = 0,
    val servingGrams: Int = 100, // current serving size in grams
    // Store original per-100g values so adjustments always calculate from the base
    val caloriesPer100g: Int = calories,
    val proteinPer100g: Int = protein,
    val fatPer100g: Int = fat,
    val carbohydratesPer100g: Int = carbohydrates
) {
    // Calculate adjusted nutrients based on serving size using the original per-100g values
    fun withServingSize(grams: Int): ScannedItem {
        val factor = grams / 100.0
        return copy(
            calories = (caloriesPer100g * factor).toInt(),
            protein = (proteinPer100g * factor).toInt(),
            fat = (fatPer100g * factor).toInt(),
            carbohydrates = (carbohydratesPer100g * factor).toInt(),
            servingGrams = grams
            // Keep the original per-100g values unchanged
        )
    }
}
