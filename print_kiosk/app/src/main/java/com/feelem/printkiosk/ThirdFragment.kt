package com.feelem.printkiosk

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class ThirdFragment : Fragment() {

    private lateinit var ivFrameOverlay: ImageView
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var tvTimer: TextView // 타이머 텍스트뷰 추가
    private lateinit var frameButtons: List<ImageButton>
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var countDownTimer: CountDownTimer? = null

    // 프레임별 배경색 정의 (라임, 핑크, 블루, 블랙 순서)
    private val frameBgColors = listOf("#C2FD76", "#FF5C8A", "#007BFF", "#000000")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPhotoPreview = view.findViewById(R.id.iv_photo_preview)
        ivFrameOverlay = view.findViewById(R.id.iv_frame_overlay)
        tvTimer = view.findViewById(R.id.tv_timer_third) // XML의 ID와 맞춰주세요

        val btnLime = view.findViewById<ImageButton>(R.id.btn_frame_lime)
        val btnPink = view.findViewById<ImageButton>(R.id.btn_frame_pink)
        val btnBlue = view.findViewById<ImageButton>(R.id.btn_frame_blue)
        val btnBlack = view.findViewById<ImageButton>(R.id.btn_frame_black)
        val btnPrint = view.findViewById<ImageButton>(R.id.btn_print)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel_print)

        frameButtons = listOf(btnLime, btnPink, btnBlue, btnBlack)

        // 1. 하드웨어 뒤로가기 버튼 처리 (안전하게 홈으로 이동)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })

        sharedViewModel.originalBitmap.observe(viewLifecycleOwner) { bitmap ->
            ivPhotoPreview.setImageBitmap(bitmap)
        }

        setupFrameSelection()

        // 2. 출력 버튼 클릭
        btnPrint.setOnClickListener {
            // 버튼을 눌렀을 때 이미지가 로드되었는지 확인
            val original = sharedViewModel.originalBitmap.value ?: return@setOnClickListener

            stopTimer() // 다음 화면으로 이동 전 타이머 정지

            val selectedIndex = frameButtons.indexOfFirst { it.isSelected }.let { if (it == -1) 0 else it }
            val currentBgColor = Color.parseColor(frameBgColors[selectedIndex])

            val combined = combineImages(original, currentBgColor)
            if (combined != null) {
                val bundle = Bundle().apply { putParcelable("printBitmap", combined) }
                findNavController().navigate(R.id.action_thirdFragment_to_loading2Fragment, bundle)
            } else {
                Toast.makeText(context, "이미지 합성 실패", Toast.LENGTH_SHORT).show()
                startTimer(60000) // 실패 시 타이머 재개
            }
        }

        // 3. 취소 버튼 클릭
        btnCancel.setOnClickListener {
            navigateToHome()
        }

        // 4. 60초 타이머 시작
        startTimer(60000)
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel() // 혹시 실행 중일지 모를 타이머 중지
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = seconds.toString()
            }

            override fun onFinish() {
                navigateToHome()
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun navigateToHome() {
        stopTimer()
        if (isAdded) {
            findNavController().navigate(R.id.action_thirdFragment_to_firstFragment)
        }
    }

    private fun setupFrameSelection() {
        val frameRes = listOf(R.drawable.frame_1, R.drawable.frame_2, R.drawable.frame_3, R.drawable.frame_4)

        frameButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                frameButtons.forEach { it.isSelected = false }
                button.isSelected = true
                ivFrameOverlay.setImageResource(frameRes[index])
                ivPhotoPreview.setBackgroundColor(Color.parseColor(frameBgColors[index]))
            }
        }
        if (frameButtons.isNotEmpty()) frameButtons[0].performClick()
    }

    private fun combineImages(photo: Bitmap, bgColor: Int): Bitmap? {
        val frame = (ivFrameOverlay.drawable as? BitmapDrawable)?.bitmap ?: return null
        val targetWidth = 638
        val targetHeight = 1016

        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawColor(bgColor)

        val scale = Math.min(targetWidth.toFloat() / photo.width, targetHeight.toFloat() / photo.height)
        val finalW = (photo.width * scale).toInt()
        val finalH = (photo.height * scale).toInt()
        val left = (targetWidth - finalW) / 2
        val top = (targetHeight - finalH) / 2

        canvas.drawBitmap(photo, null, Rect(left, top, left + finalW, top + finalH), null)
        canvas.drawBitmap(frame, null, Rect(0, 0, targetWidth, targetHeight), null)

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer() // 메모리 누수 방지를 위해 타이머 종료
    }
}