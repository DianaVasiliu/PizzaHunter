package com.android.example.pizzahunter.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.*
import com.android.example.pizzahunter.databinding.ActivityFoodDetailBinding
import com.android.example.pizzahunter.models.Food
import com.android.example.pizzahunter.ui.fragments.MenuFragment
import com.squareup.picasso.Picasso

class FoodDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailBinding

    private lateinit var food : Food
    private lateinit var type : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.title = ""
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_detail)

        food = intent.getSerializableExtra(FoodViewHolder.FOOD_OBJECT_KEY) as Food
        type = intent.getStringExtra(FoodViewHolder.FOOD_TYPE_KEY) as String

        val description = if (food.description != "") food.description else food.ingredients.joinToString(", ")
        val spicyIconVisible = if (food.spicy) View.VISIBLE else View.GONE
        val vegetarianIconVisible = if (food.vegetarian) View.VISIBLE else View.GONE

        Picasso.get().load(food.image).into(binding.imageView)
        binding.titleTextView.text = food.name
        binding.ingredientsTextView.text = description
        binding.spicyIcon.visibility = spicyIconVisible
        binding.vegetarianIcon.visibility = vegetarianIconVisible

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.food_item_action, menu)

        if (getShareIntent().resolveActivity(packageManager) == null) {
            menu?.findItem(R.id.shareButton)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.shareButton -> {
                shareFoodInfo()
                true
            }
            R.id.favouritesButton -> {
                val drawable = item.icon
                drawable.mutate()
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.secondary))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getShareIntent() : Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, getShareText())
        return shareIntent
    }

    private fun shareFoodInfo() {
        startActivity(getShareIntent())
    }

    private fun getShareText() : String {
        var text = ""

        val foodType = getFoodType()
        val description = if (food.description != "") food.description else getIngredientsList(food.ingredients)

        text += foodType
        text += food.name
        text += "\n\n"
        if (description != "") {
            text += description
            text += "\n\n"
        }
        if (food.spicy) {
            val spicyIcon = "\uD83C\uDF36"
            val spicy = getString(R.string.spicy)
            text += "$spicyIcon $spicy\n"
        }
        if (food.vegetarian) {
            val vegetarianIcon = "\uD83C\uDF43"
            val vegetarian = getString(R.string.vegetarian)
            text += "$vegetarianIcon $vegetarian\n"
        }

        val price = getString(R.string.price)
        val currency = getString(R.string.currency)
        text += "\n"
        text += "$price: ${food.price} $currency\n"

        return text
    }

    private fun getFoodType(): String {
        val foodType = when(type) {
            MenuFragment.PIZZA -> getString(R.string.pizza)
            MenuFragment.PASTA -> getString(R.string.pasta)
            MenuFragment.SALADS -> getString(R.string.salads)
            MenuFragment.SAUCES -> getString(R.string.sauces)
            else -> ""
        }
        return if (foodType != "") {
            "$foodType "
        } else {
            foodType
        }
    }

    private fun getIngredientsList(ingr : List<String>) : String {
        var text = ""
        ingr.forEach {
            text += "\u2022 $it\n"
        }
        return text
    }
}
