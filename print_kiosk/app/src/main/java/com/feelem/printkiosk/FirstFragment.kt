package com.feelem.printkiosk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // XML을 메모리에 로드(Inflate)합니다.
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 배경 이미지 뷰를 찾습니다.
        val bgImage = view.findViewById<ImageView>(R.id.iv_first_bg)

        // 2. [임시] 배경을 클릭하면 다음 화면(loading1Fragment)으로 넘어갑니다.
        // 나중에 이 부분을 "백엔드 알림 수신 시 실행" 로직으로 바꾸면 됩니다.
        bgImage.setOnClickListener {

            // 네비게이션 그래프에 정의된 화살표 ID를 사용해 이동합니다.
            // (보내주신 스크린샷의 ID와 정확히 일치시켰습니다)
            findNavController().navigate(R.id.action_firstFragment_to_loading1Fragment)
        }
    }
}