package com.android.example.pizzahunter.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.example.pizzahunter.database.Database
import com.android.example.pizzahunter.ui.fragments.HomeFragment
import com.android.example.pizzahunter.ui.fragments.InfoFragment
import com.android.example.pizzahunter.R
import com.android.example.pizzahunter.databinding.ActivityMainBinding
import com.android.example.pizzahunter.ui.fragments.MenuFragment
import com.android.example.pizzahunter.ui.fragments.ProfileLoggedInFragment
import com.android.example.pizzahunter.ui.fragments.ProfileLoggedOutFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var binding: ActivityMainBinding

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var photoUri: Uri? = null
    private var selectedPhotoUri: Uri? = null

    private var profilePicUri: Uri? = null

    private val homeFragment = HomeFragment()
    private val menuFragment = MenuFragment()
    private val infoFragment = InfoFragment()
    private val profileLoggedInFragment = ProfileLoggedInFragment()
    private val profileLoggedOutFragment = ProfileLoggedOutFragment()
    private lateinit var profileFragment: Fragment
    private var currentFragmentIndex: Int = 0
    private var currentFragment: Fragment = homeFragment

    private val getPreviewImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            // if an image has been selected from the gallery
            // load it in the cloud storage
            selectedPhotoUri = it
            loadPhoto(it)
        }
    }

    companion object {
        private const val CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX"
        private const val CURRENT_PROFILE_FRAGMENT_INDEX = "CURRENT_PROFILE_FRAGMENT_INDEX"
        private const val CAMERAX = "CAMERAX"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // save the fragment before changing the screen orientation
        // to restore the rendered fragment
        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX)
            profileFragment = when (savedInstanceState.getInt(CURRENT_PROFILE_FRAGMENT_INDEX)) {
                1 -> profileLoggedOutFragment
                else -> profileLoggedInFragment
            }
        }

        // set the initial fragment to the home fragment
        currentFragment = indexToFragment(currentFragmentIndex)
        replaceFragment(currentFragment)

        // change the profile fragment if the user logs out
        setOnAuthStateChangeListener()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.changePhotoModal.setOnClickListener {
            showChangePictureModal(false)
        }

        binding.loadPhotoButton.button.setOnClickListener {
            openGallery()
        }

        // after taking a photo, the user must chose to keep or to reject the photo
        binding.acceptPhotoButton.setOnClickListener {
            acceptPhoto()
        }
        binding.rejectPhotoButton.setOnClickListener {
            rejectPhoto()
        }

        binding.openCameraButton.button.setOnClickListener {
            if (allPermissionsGranted()) {
                checkOpenCamera(binding)
            }
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }

        binding.cameraShutter.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                animateFlash()
            }
        }

        binding.cameraBackButton.setOnClickListener {
            closeCamera()
        }

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener {
            // replace the rendered fragment when navigating
            when(it.itemId) {
                R.id.home_nav_button -> replaceFragment(homeFragment)
                R.id.menu_nav_button -> replaceFragment(menuFragment)
                R.id.info_nav_button -> replaceFragment(infoFragment)
                R.id.profile_nav_button -> replaceFragment(profileFragment)
            }
            true
        }
    }

    public override fun onStart() {
        super.onStart()
        Database.checkUserLoggedIn()

        profileFragment = if (Database.isUserLoggedIn()) {
            profileLoggedInFragment
        } else {
            profileLoggedOutFragment
        }

        if (currentFragmentIndex == 3) {
            replaceFragment(profileFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val profileIndex = when(profileFragment) {
            profileLoggedOutFragment -> 1
            else -> 2
        }

        // save information about the fragments
        // to restore the fragment when changing screen orientation
        outState.putInt(CURRENT_FRAGMENT_INDEX, currentFragmentIndex)
        outState.putInt(CURRENT_PROFILE_FRAGMENT_INDEX, profileIndex)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                checkOpenCamera(binding)
            }
            else {
                Toast.makeText(this, getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnAuthStateChangeListener() {
        Database.onAuthStateChange {
            if (Database.isUserLoggedIn()) {
                profileFragment = profileLoggedInFragment
            } else {
                profileFragment = profileLoggedOutFragment
                if (currentFragmentIndex == 3) {
                    replaceFragment(profileFragment)
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        if (!isDestroyed) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, fragment)
                commit()
            }
            currentFragment = fragment
            currentFragmentIndex = fragmentToIndex(fragment)
        }
    }

    private fun fragmentToIndex(fragment: Fragment) : Int {
        return when(fragment) {
            homeFragment -> 0
            menuFragment -> 1
            infoFragment -> 2
            profileFragment -> 3
            else -> 0
        }
    }

    private fun indexToFragment(idx: Int) : Fragment {
        return when(idx) {
            0 -> homeFragment
            1 -> menuFragment
            2 -> infoFragment
            3 -> profileFragment
            else -> homeFragment
        }
    }

    fun showLoadingScreen(value: Boolean) {
        binding.loadingScreen.visibility = if (value) View.VISIBLE else View.GONE
    }

    fun showChangePictureModal(value: Boolean) {
        binding.changePhotoModal.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun checkOpenCamera(binding: ActivityMainBinding) {
        // can only open camera if the phone is in portrait mode
        // so first check the phone orientation
        // show error if the orientation is bad
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            binding.errorModal.visibility = View.VISIBLE
            binding.errorModal.text = getString(R.string.must_be_portrait_mode)
        }
        else {
            startCamera()
        }
    }

    // required permissions: camera & write external storage
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        binding.cameraLayout.visibility = View.VISIBLE
        binding.cameraButtonsLayout.visibility = View.VISIBLE
        binding.bottomNavigation.visibility = View.GONE
        binding.errorModal.visibility = View.GONE
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT.also { this.requestedOrientation = it }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()   // front camera

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }
            catch (e: Exception) {
                Log.d(CAMERAX, "startCamera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        // set file info
        val contentValues = ContentValues().apply {
            // name, type, path
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions =
            ImageCapture
                .OutputFileOptions
                .Builder(contentResolver,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues)
                .build()

        // save the image to the gallery
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo saved to Pictures"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    photoUri = output.savedUri
                    closeCamera()
                    // show the screen where the user verifies the photo before posting it
                    checkResultedPhoto(photoUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(CAMERAX, "Photo capture failed: ${exception.message}", exception)
                }
            })
    }

    private fun closeCamera() {
        binding.cameraLayout.visibility = View.GONE
        binding.cameraButtonsLayout.visibility = View.GONE
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 150)
        }, 100)
    }

    private fun checkResultedPhoto(photoUri: Uri?) {
        if (photoUri == null) {
            return
        }
        binding.changePhotoModal.visibility = View.GONE
        binding.bottomNavigation.visibility = View.GONE
        binding.acceptPhotoLayout.visibility = View.VISIBLE
        binding.newProfilePicture.setImageURI(photoUri)
    }

    private fun rejectPhoto() {
        binding.changePhotoModal.visibility = View.GONE
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.acceptPhotoLayout.visibility = View.GONE
        binding.newProfilePicture.setImageURI(null)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    private fun acceptPhoto() {
        loadPhoto(photoUri)     // load the photo to the cloud storage
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.acceptPhotoLayout.visibility = View.GONE
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    private fun loadPhoto(uri: Uri?) {
        launch {
            binding.changePhotoModal.visibility = View.GONE
            binding.loadingScreen.visibility = View.VISIBLE

            Database.loadProfilePicture(uri)
            profilePicUri = Database.getProfilePictureUri()
            val newData = hashMapOf<String, Any?>(
                "profilePicUri" to profilePicUri.toString()
            )
            Database.updateUser(newData)

            binding.loadingScreen.visibility = View.GONE

            if (currentFragment == profileLoggedInFragment) {
                // refresh the fragment by reattaching it
                // to show the new profile pic
                var transaction = supportFragmentManager.beginTransaction()
                transaction.detach(profileLoggedInFragment)
                transaction.commit()
                transaction = supportFragmentManager.beginTransaction()
                transaction.attach(profileLoggedInFragment)
                transaction.commit()
            }
        }
    }

    private fun openGallery() {
        getPreviewImage.launch("image/*")
    }
}