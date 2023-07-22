package com.pjm.cours.ui.postlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.data.model.Post
import com.pjm.cours.databinding.ItemPostBinding

class PostListAdapter(
    private val clickListener: OnPostClickListener
) : ListAdapter<Post, PostListAdapter.PostHolder>(PostDiffUtil()) {

    class PostHolder(
        private val binding: ItemPostBinding,
        private val clickListener: OnPostClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Post) {
            binding.post = item
            binding.clickListener = clickListener
        }

        companion object {
            fun from(parent: ViewGroup, clickListener: OnPostClickListener): PostHolder {
                val binding = ItemPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return PostHolder(binding, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        return PostHolder.from(parent, clickListener)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostDiffUtil : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}