package com.feelem.printkiosk

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // Button 대신 ImageButton 임포트
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

class SecondFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var tvTimer: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // [체크] 혹시 fragment_second 가 아니라 fragment_third 인가요?
        // 아까 보여주신 XML 파일 이름에 맞춰서 확인하세요.
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString("imageUrl")

        // 1. 뷰 바인딩 시 ID와 타입을 정확히 맞춥니다.
        val imageView = view.findViewById<ImageView>(R.id.iv_user_photo)

        // [수정] XML이 ImageButton이므로 타입을 ImageButton으로 변경
        val btnPrint = view.findViewById<ImageButton>(R.id.btn_make_card)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_return_home)

        tvTimer = view.findViewById(R.id.tv_timer_number)

        // 2. 이미지 표시
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView)
        }

        // 3. 카운트다운 시작
        startTimer(60000) // 1분으로 설정

        // 4. 출력 버튼 클릭 (ThirdFragment로 이동)
        btnPrint?.setOnClickListener {
            stopTimer()
            // 1. 현재 화면(Second)이 가지고 있는 이미지 주소를 꺼냅니다.
            val currentImageUrl = arguments?.getString("imageUrl")

            // 2. 다음 화면(Third)으로 보낼 보따리(Bundle)를 만듭니다.
            val bundle = Bundle().apply {
                putString("imageUrl", currentImageUrl) // "imageUrl"이라는 이름으로 주소를 넣음
            }

            // 3. 보따리를 들고 ThirdFragment로 이동합니다.
            findNavController().navigate(R.id.action_secondFragment_to_thirdFragment, bundle)
        }

        // 5. 취소 버튼 클릭 (FirstFragment로 복귀)
        btnCancel?.setOnClickListener {
            stopTimer()
            findNavController().navigate(R.id.action_secondFragment_to_firstFragment)
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                // 초 단위 업데이트
                tvTimer.text = seconds.toString()
            }

            override fun onFinish() {
                // 안전하게 이동하기 위해 뷰가 살아있는지 확인
                if (isAdded) {
                    findNavController().navigate(R.id.action_secondFragment_to_firstFragment)
                }
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}