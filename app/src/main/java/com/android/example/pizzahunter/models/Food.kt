package com.android.example.pizzahunter.models

import java.io.Serializable

data class Food (
    val id: Int,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val spicy: Boolean,
    val vegetarian: Boolean,
    val price: Float,
    val image: String,
) : Serializable