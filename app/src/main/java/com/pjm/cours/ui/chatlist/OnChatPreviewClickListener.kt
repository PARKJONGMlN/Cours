package com.pjm.cours.ui.chatlist

import com.pjm.cours.data.model.ChatPreview

fun interface OnChatPreviewClickListener {

    fun onClick(preview: ChatPreview)
}