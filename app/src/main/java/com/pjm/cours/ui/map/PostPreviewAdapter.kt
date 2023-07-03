package com.pjm.cours.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.databinding.ItemPreviewBinding

class PreviewAdapter(
    private val clickListener: OnPreviewClickListener
) : RecyclerView.Adapter<PreviewAdapter.PreViewHolder>() {

    private val previewList = mutableListOf<PostPreview>()

    class PreViewHolder(
        private val binding: ItemPreviewBinding,
        private val clickListener: OnPreviewClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(preview: PostPreview) {
            binding.tvCategoryPreview.text = preview.category
            binding.tvLanguagePreview.text = preview.language
            binding.tvTitlePreview.text = preview.title
            binding.tvCurrentPeoplePreview.text = preview.currentMemberCount
            itemView.setOnClickListener {
                clickListener.onClick(preview)
            }
        }

        companion object {

            fun from(parent: ViewGroup, clickListener: OnPreviewClickListener): PreViewHolder {
                val binding =
                    ItemPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PreViewHolder(binding, clickListener)
            }
        }
    }

    fun submitList(items: List<PostPreview>) {
        previewList.clear()
        previewList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreViewHolder {
        return PreViewHolder.from(parent, clickListener)
    }

    override fun getItemCount(): Int {
        return previewList.size
    }

    override fun onBindViewHolder(holder: PreViewHolder, position: Int) {
        holder.bind(previewList[position])
    }
}