package com.dicoding.picodiploma.loginwithanimation.view.upload

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.api.UploadNewStoryResponse
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityUploadBinding
import com.dicoding.picodiploma.loginwithanimation.utils.Utils
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class UploadActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var utils: Utils
    private var currentImageUri: Uri? = null

    private val viewModel by viewModels<UploadViewModel>() {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        utils = Utils()

        binding.galleryButton.setOnClickListener(this)
        binding.cameraButton.setOnClickListener(this)
        binding.uploadButton.setOnClickListener(this)
    }

    private fun galeryStart() {
        galeryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        showLoading(false)
    }

    private val galeryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast("Tidak ada gambar yang dipilih")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }
    
    private fun cameraStart() {
        currentImageUri = utils.getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
        showLoading(false)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun uploadImage() {
        currentImageUri?.let {uri ->
            val imageFile = utils.uriToFile(uri, this)
            val description = "${binding.description.text}"
            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requesImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val mulitpartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requesImageFile
            )

            lifecycleScope.launch {
                try {
                    viewModel.uploadStory(mulitpartBody, requestBody).observe(this@UploadActivity, Observer { result ->
                        if (result != null) {
                            when(result) {
                                is Result.Loading -> {
                                    showLoading(true)
                                }
                                is Result.Success -> {
                                    val intent = Intent(this@UploadActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)

                                    showToast("Upload berhasil")

                                    showLoading(false)
                                }
                                is Result.Error -> {
                                    showLoading(false)
                                    showToast("Deskripsi tidak boleh kosong")
                                }
                            }
                        }
                    })

                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson<String>(errorBody, UploadNewStoryResponse::class.java)
                    showToast(errorResponse.toString())
                    showLoading(false)
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View?) {
        when(view) {
            binding.galleryButton -> {
                galeryStart()
                showLoading(true)
            }
            binding.cameraButton -> {
                cameraStart()
                showLoading(true)
            }
            binding.uploadButton -> {
                uploadImage()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        showLoading(false)
    }
}