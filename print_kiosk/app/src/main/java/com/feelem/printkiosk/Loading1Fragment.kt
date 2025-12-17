package com.feelem.printkiosk

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class Loading1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. MainActivity에서 전달받은 이미지 URL 꺼내기
        val imageUrl = arguments?.getString("imageUrl")

        if (imageUrl.isNullOrEmpty()) {
            Toast.makeText(context, "이미지 주소가 없습니다.", Toast.LENGTH_SHORT).show()
            // 에러 시 다시 홈으로 가거나 처리
            return
        }

        // 2. [실제 코드] Glide로 이미지 미리 다운로드 (Preload)
        // 화면에는 로딩바가 돌고 있고, 뒷단에서 이미지를 받습니다.
        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                // (1) 로딩 실패 시
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "이미지 로딩 실패", Toast.LENGTH_SHORT).show()
                        // 실패 시 홈으로 보내거나 재시도 버튼 띄우기
                    }
                    return false
                }

                // (2) 로딩 성공 시 (이미지 다운로드 완료!)
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // 이미지 준비 완료! 다음 화면(SecondFragment)으로 이동
                    // 약간의 딜레이가 필요하면 Handler를 여기에 쓰셔도 됩니다.
                    moveToSecondFragment(imageUrl)
                    return false
                }
            })
            .preload() // 화면에 뿌리는 게 아니라 메모리에 미리 로드
    }

    private fun moveToSecondFragment(url: String) {
        // UI 조작은 메인 스레드에서
        activity?.runOnUiThread {
            val bundle = Bundle().apply {
                putString("imageUrl", url)
            }
            // 로딩이 끝났으니 SecondFragment로 이동 (데이터 전달)
            findNavController().navigate(R.id.action_loading1Fragment_to_secondFragment, bundle)
        }
    }
}