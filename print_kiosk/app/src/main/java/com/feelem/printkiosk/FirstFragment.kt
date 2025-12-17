package com.feelem.printkiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController

class FirstFragment : Fragment() {

    // 1. 방송 수신기가 할 행동 정의
    private val printRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val imageUrl = intent?.getStringExtra("imageUrl")

            if (!imageUrl.isNullOrEmpty()) {
                Log.d("FirstFragment", "알림 수신! 로딩 화면으로 이동합니다.")

                // 보따리에 이미지 주소 담기
                val bundle = Bundle().apply {
                    putString("imageUrl", imageUrl)
                }

                // 바로 로딩 화면으로 쏘기 (현재 위치가 FirstFragment임이 보장됨)
                findNavController().navigate(R.id.action_firstFragment_to_loading1Fragment, bundle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    // ★ 핵심: 화면이 보일 때만 수신 등록 (onResume)
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(printRequestReceiver, IntentFilter("ACTION_PRINT_REQUEST"))
    }

    // ★ 핵심: 화면이 안 보이면 수신 해제 (onPause)
    // Loading이나 Second 화면으로 넘어가면 이 함수가 실행되어 수신기가 꺼집니다.
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(printRequestReceiver)
    }
}