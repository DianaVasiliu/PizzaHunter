package com.android.example.pizzahunter.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.example.pizzahunter.ui.LoginActivity
import com.android.example.pizzahunter.ui.MainActivity
import com.android.example.pizzahunter.R
import com.android.example.pizzahunter.databinding.FragmentProfileLoggedOutBinding
import com.android.example.pizzahunter.ui.SignupActivity

class ProfileLoggedOutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)
        val binding = DataBindingUtil.inflate<FragmentProfileLoggedOutBinding>(inflater,
            R.layout.fragment_profile_logged_out, container, false)

        (activity as MainActivity).showChangePictureModal(false)

        binding.loginButton.button.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signupButton.button.setOnClickListener {
            val intent = Intent(activity, SignupActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}