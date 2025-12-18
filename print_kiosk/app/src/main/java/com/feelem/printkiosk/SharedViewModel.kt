package com.feelem.printkiosk

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap

class SharedViewModel : ViewModel() {
    // 1. 가공되지 않은 순수 원본 비트맵을 담는 변수
    // LiveData를 사용하면 데이터가 변할 때 화면에 바로 알려줄 수 있습니다.
    val originalBitmap = MutableLiveData<Bitmap>()

    // 2. 비트맵을 저장하는 함수
    fun setBitmap(bitmap: Bitmap) {
        originalBitmap.value = bitmap
    }
}