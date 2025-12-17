package com.feelem.printkiosk

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

class ThirdFragment : Fragment() {

    private lateinit var ivFrameOverlay: ImageView
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var frameButtons: List<ImageButton>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPhotoPreview = view.findViewById(R.id.iv_photo_preview)
        ivFrameOverlay = view.findViewById(R.id.iv_frame_overlay)

        val btnPink = view.findViewById<ImageButton>(R.id.btn_frame_pink)
        val btnLime = view.findViewById<ImageButton>(R.id.btn_frame_lime)
        val btnBlue = view.findViewById<ImageButton>(R.id.btn_frame_blue)
        val btnBlack = view.findViewById<ImageButton>(R.id.btn_frame_black)
        val btnPrint = view.findViewById<ImageButton>(R.id.btn_print)
        val btnCancel = view.findViewById<ImageButton>(R.id.btn_cancel_print)

        // 리스트 순서를 XML 배치와 맞춤
        frameButtons = listOf(btnLime, btnPink, btnBlue, btnBlack)

        // 1. 이미지 로드 확인

        val imageUrl = arguments?.getString("imageUrl")
        android.util.Log.d("CheckData", "전달받은 URL: $imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(ivPhotoPreview)
        } else {
            // URL이 넘어오지 않았을 경우 에러 메시지
            Toast.makeText(context, "이미지 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        setupFrameSelection()

        // 2. 인쇄 버튼 동작 개선
        btnPrint.setOnClickListener {
            // 버튼을 눌렀을 때 이미지가 아직 로드 중일 수 있으므로 체크
            if (ivPhotoPreview.drawable == null) {
                Toast.makeText(context, "이미지를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val combinedBitmap = combineImages()
            if (combinedBitmap != null) {
                val bundle = Bundle().apply {
                    putParcelable("printBitmap", combinedBitmap)
                }
                findNavController().navigate(R.id.action_thirdFragment_to_loading2Fragment, bundle)
            } else {
                Toast.makeText(context, "이미지 합성 실패", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
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
            }
        }
        if (frameButtons.isNotEmpty()) frameButtons[0].performClick()
    }

    private fun combineImages(): Bitmap? {
        // 1. 소스 비트맵 가져오기
        val photo = (ivPhotoPreview.drawable as? BitmapDrawable)?.bitmap ?: return null
        val frame = (ivFrameOverlay.drawable as? BitmapDrawable)?.bitmap ?: return null

        // 2. 카드 사이즈 표준 규격 고정 (300DPI 기준 54x86mm)
        val targetWidth = 638
        val targetHeight = 1016

        // 3. 도화지(Canvas) 생성
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 4. 사진 그리기: 도화지 전체 크기에 맞춰서 배치
        val destRect = Rect(0, 0, targetWidth, targetHeight)
        canvas.drawBitmap(photo, null, destRect, null)

        // 5. 프레임 그리기: 프레임이 잘리지 않도록 '안전 마진'을 아주 미세하게 줄 수도 있습니다.
        // 만약 프레임 외곽이 계속 잘린다면 아래의 padding 값을 10~20 정도로 조절하세요.
        val padding = 0
        val frameRect = Rect(padding, padding, targetWidth - padding, targetHeight - padding)

        // 프레임을 사진 위에 1:1로 정확히 덮어씌웁니다.
        canvas.drawBitmap(frame, null, frameRect, null)

        return result
    }
}