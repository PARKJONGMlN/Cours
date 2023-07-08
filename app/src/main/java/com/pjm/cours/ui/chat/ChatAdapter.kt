package com.pjm.cours.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.data.model.ChatItem
import com.pjm.cours.data.model.MyChat
import com.pjm.cours.data.model.OtherChat
import com.pjm.cours.databinding.ItemMyChatBinding
import com.pjm.cours.databinding.ItemOtherChatBinding

private const val VIEW_TYPE_OTHER_CHAT = 0
private const val VIEW_TYPE_MY_CHAT = 1

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatItem>()

    fun submitChat(chat: ChatItem) {
        items.add(chat)
        notifyItemInserted(items.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_OTHER_CHAT -> OtherChatViewHolder.from(parent)
            else -> MyChatViewHolder.from(parent)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (holder) {
            is OtherChatViewHolder -> {
                val item = items[position] as OtherChat
                holder.bind(item)
            }
            is MyChatViewHolder -> {
                val item = items[position] as MyChat
                holder.bind(item)
            }
            else -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OtherChat -> VIEW_TYPE_OTHER_CHAT
            is MyChat -> VIEW_TYPE_MY_CHAT
        }
    }

    class OtherChatViewHolder(private val binding: ItemOtherChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OtherChat) {
            binding.tvUserNicknameOtherChat.text = item.nickname
            binding.tvMessageOtherChat.text = item.message
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
            binding.tvMessageMyChat.text = item.message
        }

        companion object {

            fun from(parent: ViewGroup): MyChatViewHolder {
                return MyChatViewHolder(
                    ItemMyChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }
    }
}