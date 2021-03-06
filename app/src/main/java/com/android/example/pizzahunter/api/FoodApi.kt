package com.android.example.pizzahunter.api

import com.android.example.pizzahunter.models.Food
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

const val BASE_URL = "https://gunter-food-api.herokuapp.com/"

interface FoodApi {
    @GET("pizza")
    fun getPizza() : Call<List<Food>>

    @GET("pasta")
    fun getPasta() : Call<List<Food>>

    @GET("salads")
    fun getSalads() : Call<List<Food>>

    @GET("dessert")
    fun getDesserts() : Call<List<Food>>

    @GET("drinks")
    fun getDrinks() : Call<List<Food>>

    @GET("sauces")
    fun getSauces() : Call<List<Food>>

    @GET("sides")
    fun getSides() : Call<List<Food>>

    companion object {
        operator fun invoke(): FoodApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FoodApi::class.java)
        }
    }
}