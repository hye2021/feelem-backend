package com.feelem.printkiosk

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FailFragment : Fragment() {

    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. XML ID 그대로 연결
        val tvReason = view.findViewById<TextView>(R.id.tv_fail_reason)
        val tvTimerText = view.findViewById<TextView>(R.id.tv_timer_fail)
        val btnHome = view.findViewById<ImageButton>(R.id.btn_return_home_fail)

        // 2. 전달받은 실패 사유 표시
        val reason = arguments?.getString("reason") ?: "프린터 연결 상태를 확인해주세요."
        tvReason.text = "[인쇄 실패]\n$reason"

        // 3. 홈 버튼 클릭
        btnHome.setOnClickListener {
            stopTimer()
            findNavController().navigate(R.id.action_failFragment_to_firstFragment)
        }

        // 4. 자동 복귀 타이머 (10초)
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimerText.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() {
                if (isAdded) findNavController().navigate(R.id.action_failFragment_to_firstFragment)
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}