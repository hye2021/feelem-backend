package com.feelem.printkiosk // [수정] 패키지명 통일

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            val type = data["type"]
            val imageUrl = data["imageUrl"]

            Log.d(TAG, "Message Received: type=$type, url=$imageUrl")

            if (type == "PRINT_REQUEST" && !imageUrl.isNullOrEmpty()) {
                handlePrintRequest(imageUrl)
            }
        }
    }

    private fun handlePrintRequest(imageUrl: String) {
        // [중요] 브로드캐스트가 아니라, 액티비티를 직접 실행하며 데이터를 넘깁니다.
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("action", "PRINT") // 액티비티에서 확인할 플래그
            putExtra("imageUrl", imageUrl)
        }
        startActivity(intent)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}