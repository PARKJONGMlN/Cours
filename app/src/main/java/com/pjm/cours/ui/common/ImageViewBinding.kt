package com.pjm.cours.ui.common

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation

@BindingAdapter("imageUriCircle")
fun loadImageCircleCrop(view: ImageView, imageUri: String?){
    Log.d("TAG", "loadImageCircleCrop: imageUr ${imageUri}")
    if(!imageUri.isNullOrEmpty()){
        view.load(imageUri) {
            transformations(CircleCropTransformation())
        }
    }
}

@BindingAdapter("imageUriRounded")
fun loadImageRoundedCorner(view: ImageView, imageUri: String?){
    if(!imageUri.isNullOrEmpty()){
        view.load(imageUri) {
            transformations(RoundedCornersTransformation())
        }
    }
}