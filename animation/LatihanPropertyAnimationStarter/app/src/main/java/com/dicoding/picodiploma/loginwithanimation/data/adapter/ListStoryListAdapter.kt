package com.dicoding.picodiploma.loginwithanimation.data.adapter

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryListEntity
import com.dicoding.picodiploma.loginwithanimation.databinding.StoryListBinding

class ListStoryListAdapter : ListAdapter<StoryListEntity, ListStoryListAdapter.MyViewHolder>(
    DIFF_CALLBACK
) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: StoryListEntity, actionCompat: Bundle?)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class MyViewHolder(binding: StoryListBinding) : RecyclerView.ViewHolder(binding.root) {

        var storyImage = binding.storyImage
        var storyTitle = binding.storyTitle
        var storyCaption = binding.storyCaption

        fun bind(storyList: StoryListEntity) {
            Glide.with(itemView.context)
                .load(storyList.photoUrl)
                .into(storyImage)
            storyTitle.text = storyList.name
            storyCaption.text = storyList.description
            Log.d("Glider", "ON")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val storyList = getItem(position)
        holder.bind(storyList)

        holder.itemView.setOnClickListener {
            val optionCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    holder.itemView.context as Activity,
                    Pair(holder.storyImage, "storyImage"),
                    Pair(holder.storyTitle, "storyTitle"),
                    Pair(holder.storyCaption, "storyCaption")
                )
            onItemClickCallback.onItemClicked(storyList, optionCompat.toBundle())

        }

    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryListEntity>() {
            override fun areItemsTheSame(oldItem: StoryListEntity, newItem: StoryListEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: StoryListEntity,
                newItem: StoryListEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}