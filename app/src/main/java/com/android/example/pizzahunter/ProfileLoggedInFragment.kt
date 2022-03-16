package com.android.example.pizzahunter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.example.pizzahunter.databinding.FragmentProfileLoggedInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class ProfileLoggedInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)
        val binding = DataBindingUtil.inflate<FragmentProfileLoggedInBinding>(inflater, R.layout.fragment_profile_logged_in, container, false)

        lifecycleScope.launch {
            if (Database.isUserLoggedIn()) {
                val user = Database.getUser()
                val greeting = getString(R.string.greeting) + ", ${user?.get("firstName")}!"
                binding.greetingText.text = greeting
            }
            else {
                val greeting = getString(R.string.greeting) + "!"
                binding.greetingText.text = greeting
            }
        }

        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()

        val googleSignInClient = GoogleSignIn.getClient(activity as AppCompatActivity, gso)

        binding.logoutButton.setOnClickListener {
            Database.signOut()
            googleSignInClient.revokeAccess()
        }

        return binding.root
    }

}