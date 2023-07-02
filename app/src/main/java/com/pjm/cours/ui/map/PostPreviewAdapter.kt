package com.pjm.cours.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.databinding.ItemPreviewBinding

class PreviewAdapter : RecyclerView.Adapter<PreviewAdapter.PreViewHolder>() {

    private val previewList = mutableListOf<String>()

    class PreViewHolder(private val binding: ItemPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(preview: String) {
            binding.tvTitlePreview.text = preview
        }

        companion object {

            fun from(parent: ViewGroup): PreViewHolder {
                val binding =
                    ItemPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PreViewHolder(binding)
            }
        }
    }

    fun submitList(items: List<String>) {
        previewList.clear()
        previewList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreViewHolder {
        return PreViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return previewList.size
    }

    override fun onBindViewHolder(holder: PreViewHolder, position: Int) {
        holder.bind(previewList[position])
    }
}