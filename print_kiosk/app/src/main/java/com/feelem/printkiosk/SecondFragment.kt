package com.feelem.printkiosk

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class SecondFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var tvTimer: TextView
    // ViewModel 연결 (activityViewModels 사용 필수)
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString("imageUrl")
        val imageView = view.findViewById<ImageView>(R.id.iv_user_photo)
        val btnPrint = view.findViewById<ImageButton>(R.id.btn_make_card)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_return_home)
        tvTimer = view.findViewById(R.id.tv_timer_number)

        // [수정] 이미지를 로드하면서 동시에 원본 비트맵을 ViewModel에 저장
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        imageView.setImageBitmap(resource)
                        sharedViewModel.setBitmap(resource) // 원본 보관
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        startTimer(60000)

        btnPrint?.setOnClickListener {
            stopTimer()
            findNavController().navigate(R.id.action_secondFragment_to_thirdFragment)
        }

        btnCancel?.setOnClickListener {
            stopTimer()
            findNavController().navigate(R.id.action_secondFragment_to_firstFragment)
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = (millisUntilFinished / 1000).toString()
            }
            override fun onFinish() {
                if (isAdded) findNavController().navigate(R.id.action_secondFragment_to_firstFragment)
            }
        }.start()
    }

    private fun stopTimer() { countDownTimer?.cancel() }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }
}