package com.feelem.printkiosk

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val type = remoteMessage.data["type"]
            val imageUrl = remoteMessage.data["imageUrl"]

            if (type == "PRINT_REQUEST" && !imageUrl.isNullOrEmpty()) {
                sendBroadcastToFragment(imageUrl)
            }
        }
    }

    private fun sendBroadcastToFragment(imageUrl: String) {
        // [변경] 앱 내부의 누구든지 들어라! 하고 방송을 보냅니다.
        val intent = Intent("ACTION_PRINT_REQUEST") // 채널 이름
        intent.putExtra("imageUrl", imageUrl)

        // 로컬 브로드캐스트 발송
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}