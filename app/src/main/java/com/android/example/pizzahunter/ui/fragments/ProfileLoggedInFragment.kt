package com.android.example.pizzahunter.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.example.pizzahunter.database.Database
import com.android.example.pizzahunter.ui.MainActivity
import com.android.example.pizzahunter.R
import com.android.example.pizzahunter.databinding.FragmentProfileLoggedInBinding
import com.android.example.pizzahunter.ui.EditProfileActivity
import com.android.example.pizzahunter.ui.SignupActivity
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ProfileLoggedInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)
        val binding = DataBindingUtil.inflate<FragmentProfileLoggedInBinding>(inflater,
            R.layout.fragment_profile_logged_in, container, false)

        (activity as MainActivity).showChangePictureModal(false)

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.profileImage.setOnClickListener {
            (activity as MainActivity).showChangePictureModal(true)
        }

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
            val mainActivity = requireActivity() as MainActivity
            mainActivity.showLoadingScreen(true)
            Database.signOut()
            googleSignInClient.revokeAccess()
            LoginManager.getInstance().logOut()
            mainActivity.showLoadingScreen(false)
        }

        lifecycleScope.launch {
            var imageUrl: String = Database.getUser()?.get("profilePicUri").toString()

            if (imageUrl.indexOf("facebook") != -1) {
                val facebookAccessToken = AccessToken.getCurrentAccessToken()?.token
                imageUrl = "$imageUrl?access_token=$facebookAccessToken"
            }

            if (imageUrl != "null" && imageUrl != "") {
                Picasso.get().load(imageUrl).into(binding.profileImageImageview)
            }
        }

        return binding.root
    }
}