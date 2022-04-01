package com.android.example.pizzahunter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.pizzahunter.databinding.FragmentMenuBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuFragment : Fragment() {
    lateinit var binding: FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.menu_title)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)

        (activity as MainActivity).showChangePictureModal(false)

        binding.refreshLayout.setOnRefreshListener {
            fetchFood()
        }

        fetchFood()

        return binding.root
    }

    private fun fetchFood() {
        binding.refreshLayout.isRefreshing = true

        FoodApi().getPizza().enqueue(object : Callback<List<Food>> {
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