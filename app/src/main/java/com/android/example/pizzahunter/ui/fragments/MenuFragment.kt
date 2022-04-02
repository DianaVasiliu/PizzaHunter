package com.android.example.pizzahunter.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.pizzahunter.*
import com.android.example.pizzahunter.api.FoodApi
import com.android.example.pizzahunter.databinding.FragmentMenuBinding
import com.android.example.pizzahunter.models.Food
import com.android.example.pizzahunter.ui.FoodAdapter
import com.android.example.pizzahunter.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuFragment : Fragment() {
    lateinit var binding: FragmentMenuBinding
    private var selectedMenuItem = MenuItems.PIZZA
    private val foodApi = FoodApi()

    object MenuItems {
        const val PIZZA = "pizza"
        const val PASTA = "pasta"
        const val SALADS = "salads"
        const val DESSERTS = "desserts"
        const val DRINKS = "drinks"
        const val SAUCES = "sauces"
        const val SIDES = "sides"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.menu_title)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)

        (activity as MainActivity).showChangePictureModal(false)

        fetchFood()

        binding.refreshLayout.setOnRefreshListener {
            fetchFood()
        }

        val menuButtons = listOf(
            binding.pizzaMenuButton,
            binding.pastaMenuButton,
            binding.saladsMenuButton,
            binding.dessertMenuButton,
            binding.drinksMenuButton,
            binding.saucesMenuButton,
            binding.sidesMenuButton
        )

        for ((index, menuItem) in menuButtons.withIndex()) {
            menuItem.setOnClickListener {
                selectedMenuItem = when(index) {
                    0 -> MenuItems.PIZZA
                    1 -> MenuItems.PASTA
                    2 -> MenuItems.SALADS
                    3 -> MenuItems.DESSERTS
                    4 -> MenuItems.DRINKS
                    5 -> MenuItems.SAUCES
                    6 -> MenuItems.SIDES
                    else -> MenuItems.PIZZA
                }
                fetchFood()
            }
        }

        return binding.root
    }

    private fun fetchFood() {
        binding.refreshLayout.isRefreshing = true

        val fetchFunction = when(selectedMenuItem) {
            MenuItems.PIZZA -> foodApi.getPizza()
            MenuItems.PASTA -> foodApi.getPasta()
            MenuItems.SALADS -> foodApi.getSalads()
            MenuItems.DESSERTS -> foodApi.getDesserts()
            MenuItems.DRINKS -> foodApi.getDrinks()
            MenuItems.SAUCES -> foodApi.getSauces()
            MenuItems.SIDES -> foodApi.getSides()
            else -> foodApi.getPizza()
        }

        fetchFunction.enqueue(object : Callback<List<Food>> {
            override fun onResponse(call: Call<List<Food>>, response: Response<List<Food>>) {
                binding.refreshLayout.isRefreshing = false
                val food = response.body()

                food?.let {
                    showFood(food)
                }
            }

            override fun onFailure(call: Call<List<Food>>, t: Throwable) {
                binding.refreshLayout.isRefreshing = false
                Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showFood(food: List<Food>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = FoodAdapter(food)
    }
}