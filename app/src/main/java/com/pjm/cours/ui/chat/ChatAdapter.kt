package com.pjm.cours.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.data.model.ChatItem
import com.pjm.cours.data.model.MyChat
import com.pjm.cours.data.model.OtherChat
import com.pjm.cours.databinding.ItemMyChatBinding
import com.pjm.cours.databinding.ItemOtherChatBinding

private const val VIEW_TYPE_OTHER_CHAT = 0
private const val VIEW_TYPE_MY_CHAT = 1

class ChatAdapter : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_OTHER_CHAT -> OtherChatViewHolder.from(parent)
            else -> MyChatViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (holder) {
            is OtherChatViewHolder -> {
                val item = getItem(position) as OtherChat
                holder.bind(item)
            }
            is MyChatViewHolder -> {
                val item = getItem(position) as MyChat
                holder.bind(item)
            }
            else -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is OtherChat -> VIEW_TYPE_OTHER_CHAT
            is MyChat -> VIEW_TYPE_MY_CHAT
        }
    }

    class OtherChatViewHolder(private val binding: ItemOtherChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OtherChat) {
            binding.tvUserNicknameOtherChat.text = item.sender
            binding.tvMessageOtherChat.text = item.text
        }

        companion object {

            fun from(parent: ViewGroup): OtherChatViewHolder {
                return OtherChatViewHolder(
                    ItemOtherChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }
    }

    class MyChatViewHolder(private val binding: ItemMyChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyChat) {
            binding.tvMessageMyChat.text = item.text
        }

        companion object {

            fun from(parent: ViewGroup): MyChatViewHolder {
                return MyChatViewHolder(
                    ItemMyChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }
    }
}