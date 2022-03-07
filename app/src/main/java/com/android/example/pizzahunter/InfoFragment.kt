package com.android.example.pizzahunter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.databinding.FragmentInfoBinding
import com.android.example.pizzahunter.databinding.InfoButtonBinding

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.info_title)

        val binding = DataBindingUtil.inflate<FragmentInfoBinding>(inflater, R.layout.fragment_info, container, false)

        val infoButtons : MutableList<InfoButtonBinding> = mutableListOf(
            binding.locationButton,
            binding.scheduleButton,
            binding.minorderButton,
            binding.termsButton,
            binding.allergensButton
        )

        for ((index, button) in infoButtons.withIndex()) {
            button.buttonIcon = index
            // add click event
        }

        return binding.root
    }
}