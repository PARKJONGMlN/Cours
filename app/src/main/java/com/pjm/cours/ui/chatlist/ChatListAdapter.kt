package com.pjm.cours.ui.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.pjm.cours.data.model.ChatPreview
import com.pjm.cours.databinding.ItemChatPreviewBinding
import com.pjm.cours.util.DateFormat

class ChatListAdapter(
    private val clickListener: OnChatPreviewClickListener
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private val items = mutableListOf<ChatPreview>()

    fun submitFirst(previewList: List<ChatPreview>) {
        items.addAll(previewList)
        notifyDataSetChanged()
    }

    fun submitItem(preview: ChatPreview) {
        val item = items.find { it.postId == preview.postId }
        val title = item?.postTitle ?: ""
        val hostImageUri = item?.hostImageUri ?: ""
        if (item != null) {
            val index = items.indexOf(item)
            items.removeAt(index)
            notifyItemRemoved(index)
        }
        items.add(0, preview.copy(hostImageUri = hostImageUri, postTitle = title))
        notifyItemInserted(0)
    }

    class ChatListViewHolder(
        private val binding: ItemChatPreviewBinding,
        private val clickListener: OnChatPreviewClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(preview: ChatPreview) {
            binding.ivHostProfileImageChatPreview.load(preview.hostImageUri) {
                transformations(RoundedCornersTransformation())
            }
            binding.tvLastChatPreview.text = preview.lastMessage
            binding.tvTitleChatPreview.text = preview.postTitle
            binding.tvUnreadChatPreview.text = preview.unReadMessageCount
            binding.tvLastChatDatePreview.text = DateFormat.convertTimestamp(preview.messageDate)
            itemView.setOnClickListener {
                clickListener.onClick(preview)
            }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        return ChatListViewHolder.from(parent, clickListener)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(items[position])
    }
}