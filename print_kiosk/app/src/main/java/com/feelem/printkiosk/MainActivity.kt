package com.feelem.printkiosk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 우리가 만든 '빈 액자'인 activity_main.xml을 화면에 띄웁니다.
        // 이 xml 안에 있는 <FragmentContainerView>가
        // nav_graph 설정에 따라 첫 화면(FirstFragment)을 자동으로 보여줍니다.
        setContentView(R.layout.activity_main)

        // [키오스크용 팁] 상단 타이틀바(Action Bar) 없애기
        // 키오스크는 보통 전체화면이므로 타이틀바를 숨기는 게 좋습니다.
        supportActionBar?.hide()
    }
}