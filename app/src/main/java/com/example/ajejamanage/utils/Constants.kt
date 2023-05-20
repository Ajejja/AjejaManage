package com.example.ajejamanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.ajejamanage.activities.MyProfileActivity

object Constants{
    const val USERS:String="users"
    const val Boards:String="Boards"
    const val Image:String="image"
    const val Name:String="name"
    const val Mobile:String="mobile"
    const val assignedTo:String="assignedTo"
     const val READ_STORAGE_PERMISSION_CODE=1
     const val PICK_IMAGE_REQUEST_CODE=2
    const val DOCUMENT_ID:String="documentId"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAIL:String="board_detail"
    const val ID:String="id"
    const val EMAIL: String = "email"
    const val TASK_LIST_ITEM_POSITION:String="task_list_item_adapter"
    const val CARD_LIST_ITEM_POSITION:  String="card_list_item_position"
    const val BOARD_MEMBERS_LIST:String="board_member_list"
    const val SELECT:String="Select"
    const val UN_SELECT:String="Unselected"
    const val AJEJAMANAGE_PREFERENCES="ajejamanage_preferences"
    const val FCM_TOKEN_UPDATED="fcmTokenUpdated"
    const val FCM_TOKEN="fcmToken"
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAxvL0rWw:APA91bGkwBQ2pI4CiWD9coiQ43Zhnz_BJu3B60b1Ffpt8JQ9i92Gksk4WlnXW5jHfBF1vhmVLHZNt2-ZBb6yYEMYFZWtgn-2-8g3yX8VoQkhEnKfrYPicsruBCYuCTsoE_HZZGi5PIVw"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    fun showImageChooser(activity:Activity){
        var galleryIIntent= Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIIntent, PICK_IMAGE_REQUEST_CODE)
    }
     fun getFileExtension(activity:Activity,uri: Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!! ))
    }
}