package com.android.example.pizzahunter.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private lateinit var binding: FragmentMenuBinding
    private var food : List<Food>? = listOf()
    private lateinit var filteredFoodList : MutableList<Food>
    private var searchButton: SearchView? = null

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

        filteredFoodList = mutableListOf()

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
                searchButton?.setQuery("", false)
                searchButton?.isIconified = true
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

        setHasOptionsMenu(true)

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
                food = response.body()

                food?.let {
                    filteredFoodList.clear()
                    filteredFoodList.addAll(it)
                    showFood()
                }
            }

            override fun onFailure(call: Call<List<Food>>, t: Throwable) {
                binding.refreshLayout.isRefreshing = false
                Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showFood() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = FoodAdapter(filteredFoodList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_action, menu)

        searchButton = menu.findItem(R.id.search_action).actionView as SearchView

        searchButton?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()
                searchButton?.clearFocus()
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                filteredFoodList.clear()
                val searchText = newText!!.lowercase()

                if (searchText.isNotEmpty()) {
                    food?.forEach {
                        val name = it.name.lowercase()
                        val description = it.description.lowercase()
                        val ingredients = it.ingredients.joinToString(",").lowercase()

                        if(name.contains(searchText) ||
                            description.contains(searchText) ||
                            ingredients.contains(searchText)) {

                            filteredFoodList.add(it)
                        }
                    }
                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                }
                else {
                    filteredFoodList.clear()
                    food?.let { filteredFoodList.addAll(it) }
                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDetach() {
        super.onDetach()
        selectedMenuItem = MenuItems.PIZZA
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }
}