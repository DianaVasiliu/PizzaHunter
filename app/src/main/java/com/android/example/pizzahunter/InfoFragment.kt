package com.android.example.pizzahunter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<FragmentInfoBinding>(inflater, R.layout.fragment_info, container, false)

        binding.locationButton.buttonIcon = 0
        binding.scheduleButton.buttonIcon = 1
        binding.minorderButton.buttonIcon = 2
        binding.termsButton.buttonIcon = 3
        binding.allergensButton.buttonIcon = 4

        return binding.root
    }
}