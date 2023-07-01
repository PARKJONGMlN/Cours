package com.pjm.cours.ui.postcomposition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pjm.cours.databinding.ItemDialogBinding

class ItemPickAdapter(
    private val itemList: List<String>,
    private val clickListener: OnCategoryClickListener
) :
    RecyclerView.Adapter<ItemPickAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(
        private val binding: ItemDialogBinding,
        private val clickListener: OnCategoryClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.tvItem.text = category
            itemView.setOnClickListener {
                clickListener.onClick(category)
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                clickListener: OnCategoryClickListener
            ): CategoryViewHolder {
                val binding =
                    ItemDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CategoryViewHolder(binding, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryViewHolder.from(parent, clickListener)

    override fun getItemCount() = itemList.size
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(itemList[position])
    }
}