package com.pjm.cours.ui.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.databinding.ItemChatPreviewBinding

class ChatListAdapter(
    private val clickListener: OnChatPreviewClickListener
) : ListAdapter<ChatPreview, ChatListAdapter.ChatListViewHolder>(ChatPreviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        return ChatListViewHolder.from(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class ChatListViewHolder(
        private val binding: ItemChatPreviewBinding,
        private val clickListener: OnChatPreviewClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(preview: ChatPreview) {
            binding.chatPreview = preview
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {

            fun from(
                parent: ViewGroup,
                clickListener: OnChatPreviewClickListener
            ): ChatListViewHolder {
                return ChatListViewHolder(
                    ItemChatPreviewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), clickListener
                )
            }
        }
    }

    class ChatPreviewDiffCallback : DiffUtil.ItemCallback<ChatPreview>() {
        override fun areItemsTheSame(oldItem: ChatPreview, newItem: ChatPreview): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: ChatPreview, newItem: ChatPreview): Boolean {
            return oldItem == newItem
        }
    }
}