package com.feelem.printkiosk

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class Loading1Fragment : Fragment() {

    private lateinit var lottieLoading: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lottieLoading = view.findViewById(R.id.lottie_loading1)

        // 1. 이미지 URL 가져오기
        val imageUrl = arguments?.getString("imageUrl")

        if (imageUrl.isNullOrEmpty()) {
            Toast.makeText(context, "이미지 주소가 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_loading1Fragment_to_firstFragment)
            return
        }

        // 2. Glide로 이미지 프리로드
        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loading1Fragment_to_firstFragment)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 준비 완료 시 다음 화면으로 이동
                    moveToSecondFragment(imageUrl)
                    return false
                }
            })
            .preload()
    }

    private fun moveToSecondFragment(url: String) {
        activity?.runOnUiThread {
            if (isAdded) {
                val bundle = Bundle().apply {
                    putString("imageUrl", url)
                }
                findNavController().navigate(R.id.action_loading1Fragment_to_secondFragment, bundle)
            }
        }
    }
}