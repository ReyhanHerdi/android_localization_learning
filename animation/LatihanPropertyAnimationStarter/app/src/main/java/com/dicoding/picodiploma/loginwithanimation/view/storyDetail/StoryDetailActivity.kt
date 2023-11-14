package com.dicoding.picodiploma.loginwithanimation.view.storyDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityStoryDetailBinding

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(true)
        setStoryDetailData()
    }

    private fun setStoryDetailData()
    {
        binding.storyTitle.text = intent.getStringExtra(NAME)
        binding.storyCaption.text = intent.getStringExtra(DESCRIPTION)
        binding.storyDate.text = intent.getStringExtra(DATE_ADDED)
        Glide.with(this@StoryDetailActivity)
            .load(intent.getStringExtra(IMAGE))
            .into(binding.imageView)
        showLoading(false)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val TOKEN = "token"
        const val ID_STORY = "id_story"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val DATE_ADDED = "date_added"
        const val IMAGE = "image"
    }
}