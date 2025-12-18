package com.feelem.printkiosk

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기 실행 시 시스템 UI 숨김
        hideSystemUI()
    }

    /**
     * 앱에 포커스가 돌아올 때마다 호출됩니다.
     * 인쇄 다이얼로그(외부 앱)에서 돌아오는 시점을 감지하여
     * 다시 나타난 내비게이션 바를 즉시 숨깁니다.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * 내비게이션 바와 상태 바를 완전히 숨기고
     * 스와이프 시에만 잠시 나타나는 몰입 모드(Immersive Mode)를 설정합니다.
     */
    fun hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11 (API 30) 이상 최신 방식
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                // 상태바와 네비게이션바 숨기기
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                // 스와이프하면 잠깐 보였다가 다시 자동으로 사라지는 동작
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 이하 구버전 방식
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }
}