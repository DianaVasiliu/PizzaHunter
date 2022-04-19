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

    private var selectedMenuItem = PIZZA
    private val foodApi = FoodApi()

    companion object MenuItems {
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

        // initialize with empty list
        filteredFoodList = mutableListOf()

        // fetch food from the api
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

        // setting on click listener for the buttons in the top slider
        for ((index, menuItem) in menuButtons.withIndex()) {
            menuItem.setOnClickListener {
                // reset the search button: delete the text and show the search icon
                searchButton?.setQuery("", false)
                searchButton?.isIconified = true
                // set the menu item according to the clicked button
                selectedMenuItem = when(index) {
                    0 -> PIZZA
                    1 -> PASTA
                    2 -> SALADS
                    3 -> DESSERTS
                    4 -> DRINKS
                    5 -> SAUCES
                    6 -> SIDES
                    else -> PIZZA
                }
                fetchFood()
            }
        }

        // for the search button
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun fetchFood() {
        // show the loading progress bar
        binding.refreshLayout.isRefreshing = true

        // call the api methods, according to the selected menu item
        val fetchFunction = when(selectedMenuItem) {
            PIZZA -> foodApi.getPizza()
            PASTA -> foodApi.getPasta()
            SALADS -> foodApi.getSalads()
            DESSERTS -> foodApi.getDesserts()
            DRINKS -> foodApi.getDrinks()
            SAUCES -> foodApi.getSauces()
            SIDES -> foodApi.getSides()
            else -> foodApi.getPizza()
        }

        // enqueue - send the request async
        fetchFunction.enqueue(object : Callback<List<Food>> {
            override fun onResponse(call: Call<List<Food>>, response: Response<List<Food>>) {
                binding.refreshLayout.isRefreshing = false
                food = response.body()

                food?.let {
                    // clear the old data in the list
                    // and add the new fetch data
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
        // send the new parameters to the food manager
        binding.recyclerView.adapter = FoodAdapter(filteredFoodList, selectedMenuItem)
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
                // clear the filtered list if there is a new query text in the search bar
                filteredFoodList.clear()
                val searchText = newText!!.lowercase()

                if (searchText.isNotEmpty()) {
                    food?.forEach {
                        val name = it.name.lowercase()
                        val description = it.description.lowercase()
                        val ingredients = it.ingredients.joinToString(",").lowercase()

                        // checking if the query text exists in any food info
                        if(name.contains(searchText) ||
                            description.contains(searchText) ||
                            ingredients.contains(searchText)) {

                            // add the food item in the filtered list if it matches the query text
                            filteredFoodList.add(it)
                        }
                    }
                    // change the recyclerview data
                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                }
                else {
                    // if the query text has been cleared
                    // then reset the filtered food list to contain all the fetched food
                    filteredFoodList.clear()
                    food?.let { filteredFoodList.addAll(it) }
                    // change the recyclerview data
                    binding.recyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDetach() {
        super.onDetach()
        selectedMenuItem = PIZZA
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }
}