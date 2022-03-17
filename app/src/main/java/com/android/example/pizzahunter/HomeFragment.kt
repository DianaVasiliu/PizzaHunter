package com.android.example.pizzahunter

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.example.pizzahunter.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.home_title)
        val binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false)

        (activity as MainActivity).showChangePictureModal(false)

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

        binding.videoView.apply {
            setVideoPath(
                "android.resource://" +
                        (activity as AppCompatActivity).packageName +
                        "/" + R.raw.pizza
            )
            setMediaController(null)
            requestFocus()
        }

        binding.videoView.seekTo(10)

        binding.videoView.setOnCompletionListener {
            binding.videoView.seekTo(0)
            binding.videoView.background = Drawable.createFromPath(
                (activity as AppCompatActivity).packageName +
                        "/" + R.mipmap.thumbnail)
        }

        binding.videoView.setOnClickListener {
            binding.videoView.background = null
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.playButton.visibility = View.VISIBLE
            }
            else {
                binding.videoView.start()
                binding.playButton.visibility = View.GONE
            }
        }

        return binding.root
    }

}