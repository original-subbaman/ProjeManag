package com.example.projemanag.utility

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {

    const val USERS: String = "users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val EMAIL: String = "email"

    const val BOARDS: String = "boards"
    const val ASSIGNED_TO: String = "assignedTo"

    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"

    const val BOARD_DETAIL: String = "board_details"
    const val UID: String = "uid"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBERS_LIST: String = "board_members_list"
    const val SELECT: String = "Select"
    const val UNSELECT: String = "Unselect"

    const val PROJEMANAG_PREFERENCES: String = "projemanag_preferences"
    const val FCM_TOKEN_UPDATED: String = "fcm_token_updated"
    const val FCM_TOKEN: String = "fcmToken"


    fun getFileExtension(uri: Uri?, activity: Activity): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}