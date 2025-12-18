package com.feelem.printkiosk

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SuccessFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var tvTimer: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTimer = view.findViewById(R.id.tv_timer_success)
        val btnHome = view.findViewById<ImageButton>(R.id.btn_return_home_success)

        // 1. 하드웨어 뒤로가기 버튼 차단 (처음으로 이동)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })

        // 2. 홈으로 돌아가기 버튼 클릭 이벤트
        btnHome.setOnClickListener {
            navigateToHome()
        }

        // 3. 10초 자동 종료 타이머 시작
        startExitTimer(10000) // 10초
    }

    private fun startExitTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 초 단위로 텍스트 업데이트
                val seconds = millisUntilFinished / 1000
                tvTimer.text = seconds.toString()
            }

            override fun onFinish() {
                // 시간이 다 되면 자동으로 홈으로 이동
                navigateToHome()
            }
        }.start()
    }

    private fun navigateToHome() {
        stopTimer()
        if (isAdded) {
            // 모든 백스택을 비우고 FirstFragment로 이동하는 것이 가장 안전합니다.
            findNavController().navigate(R.id.action_successFragment_to_firstFragment)
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onResume() {
        super.onResume()
        // 인쇄 서비스에서 돌아왔을 때 다시 내비게이션 바 숨기기
        (activity as? MainActivity)?.hideSystemUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer() // 프래그먼트가 파괴될 때 타이머도 확실히 종료
    }
}