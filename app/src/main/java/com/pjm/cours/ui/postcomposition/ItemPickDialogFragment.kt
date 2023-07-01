package com.pjm.cours.ui.postcomposition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.pjm.cours.databinding.DialogItemPickBinding

class ItemPickDialogFragment(
    private val dialogTitle: String,
    private val itemList: List<String>,
    private val clickListener: OnCategoryClickListener
) : DialogFragment() {

    private var _binding: DialogItemPickBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemPickAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ItemPickAdapter(itemList) {
            clickListener.onClick(it)
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogItemPickBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDialogTitleCategory.text = dialogTitle
        binding.recyclerViewCategory.adapter = adapter
        binding.recyclerViewCategory.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}