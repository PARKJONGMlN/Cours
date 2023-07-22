package com.pjm.cours.ui.postlist

import com.pjm.cours.data.model.Post

fun interface OnPostClickListener {

    fun onClick(post: Post)
}