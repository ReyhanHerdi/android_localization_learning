package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.data.adapter.ListStoryListAdapter
import com.dicoding.picodiploma.loginwithanimation.view.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.storyDetail.StoryDetailActivity
import com.dicoding.picodiploma.loginwithanimation.view.upload.UploadActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        setupView()

        lifecycleScope.launch {
            setAdapter()
        }
        setupAction()

        binding.uploadImage.setOnClickListener(this)
        binding.mapsButton.setOnClickListener(this)
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private suspend fun setAdapter() {
        val storiesAdapter = ListStoryListAdapter()

        viewModel.getStoryList().observe(this, Observer { result ->
            if (result != null) {
                when(result) {
                    is Result.Loading -> {
                        Log.d("Progres", "Masih loading")
                    }
                    is Result.Success -> {
                        val storiesData = result.data
                        storiesAdapter.submitList(storiesData)
                    }
                    is Result.Error -> {
                        Toast.makeText(this@MainActivity, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        binding.storyList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = storiesAdapter
        }

        storiesAdapter.setOnItemClickCallback(object : ListStoryListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: StoryListEntity, actionCompat: Bundle?) {
                lifecycleScope.launch {
                    getStoryDetail(data, actionCompat)
                }
            }

        })
    }

    private suspend fun getStoryDetail(storyListEntity: StoryListEntity, actionCompat: Bundle?) {
        val token = viewModel.getToken()
        Log.d("token in activ", token)
        val intent = Intent(this@MainActivity, StoryDetailActivity::class.java)
        intent.putExtra(StoryDetailActivity.ID_STORY, storyListEntity.id)
        intent.putExtra(StoryDetailActivity.TOKEN, token)
        intent.putExtra(StoryDetailActivity.NAME, storyListEntity.name)
        intent.putExtra(StoryDetailActivity.DESCRIPTION, storyListEntity.description)
        intent.putExtra(StoryDetailActivity.DATE_ADDED, storyListEntity.createdAt)
        intent.putExtra(StoryDetailActivity.IMAGE, storyListEntity.photoUrl)
        startActivity(intent, actionCompat)
    }

    companion object {
        const val TOKEN = "token"
    }


    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onClick(view: View?) {
        when(view) {
            binding.uploadImage -> {
                lifecycleScope.launch {
                    val token = viewModel.getToken()
                    val intent = Intent(this@MainActivity, UploadActivity::class.java)
                    intent.putExtra(TOKEN, token)
                    startActivity(intent)
                }
            }
            binding.mapsButton -> {
                intent = Intent(this@MainActivity, MapsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}