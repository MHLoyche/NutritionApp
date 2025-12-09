package com.example.nutritionapptwo.model

data class ScannedItem(
    val barcode: String,
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbohydrates: Int = 0
)