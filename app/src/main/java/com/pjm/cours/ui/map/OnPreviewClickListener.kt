package com.pjm.cours.ui.map

import com.pjm.cours.data.model.PostPreview

fun interface OnPreviewClickListener {

    fun onClick(preview: PostPreview)
}