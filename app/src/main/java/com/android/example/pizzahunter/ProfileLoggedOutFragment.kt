package com.android.example.pizzahunter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.databinding.FragmentProfileLoggedOutBinding

class ProfileLoggedOutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)
        val binding = DataBindingUtil.inflate<FragmentProfileLoggedOutBinding>(inflater, R.layout.fragment_profile_logged_out, container, false)

        return binding.root
    }
}