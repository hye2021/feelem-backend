package com.feelem.printkiosk

import android.os.Bundle
import android.os.Looper
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.postDelayed
import androidx.navigation.fragment.findNavController

class Loading1Fragment : Fragment() {
    // ... onCreateView 생략

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3초(3000ms) 뒤에 실행되는 코드 (Handler 사용)
        Handler(Looper.getMainLooper()).postDelayed({

            // 로딩이 끝나면 SecondFragment로 이동
            // (만약 현재 화면이 여전히 이 Fragment일 때만 이동하도록 안전장치 필요할 수 있음)
            findNavController().navigate(R.id.action_loading1Fragment_to_secondFragment)

        }, 3000)
    }
}