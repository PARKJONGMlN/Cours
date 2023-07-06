package com.pjm.cours.ui.postdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.pjm.cours.databinding.DialogCheckRegisterBinding

class CheckRegisterDialogFragment(
    private val clickListener: OnRegisterClickListener
) : DialogFragment() {

    private var _binding: DialogCheckRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCheckRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvCancelRegister.setOnClickListener {
            dismiss()
        }
        binding.cvCompleteRegister.setOnClickListener {
            clickListener.onClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}