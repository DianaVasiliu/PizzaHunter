package com.android.example.pizzahunter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.example.pizzahunter.R
import com.android.example.pizzahunter.databinding.LayoutFoodBinding
import com.android.example.pizzahunter.models.Food
import com.squareup.picasso.Picasso

class FoodAdapter(private val foods: List<Food>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(
            LayoutFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = foods.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foods[position]
        val currency = holder.binding.imageView.context.resources.getString(R.string.currency)

        val description = if (food.description != "") food.description else food.ingredients.joinToString(", ")
        val price = food.price.toString() + " " + currency

        Picasso.get().load(food.image).into(holder.binding.imageView)
        holder.binding.textViewName.text = food.name
        holder.binding.textViewDescription.text = description
        holder.binding.chilliIcon.visibility = if (food.spicy) View.VISIBLE else View.GONE
        holder.binding.vegetarianIcon.visibility = if (food.vegetarian) View.VISIBLE else View.GONE
        holder.binding.priceValue.text = price
    }

    inner class FoodViewHolder(val binding: LayoutFoodBinding) : RecyclerView.ViewHolder(binding.root)

}