package com.pjm.cours.ui.common

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.CircleCropTransformation

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, imageUri: String?){
    if(!imageUri.isNullOrEmpty()){
        view.load(imageUri) {
            transformations(CircleCropTransformation())
        }
    }
}