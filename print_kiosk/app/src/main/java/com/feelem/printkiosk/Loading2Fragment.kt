package com.feelem.printkiosk

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintJob
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.print.PrintHelper
import kotlinx.coroutines.*

class Loading2Fragment : Fragment() {

    private var printJob: PrintJob? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loading2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bitmap = arguments?.getParcelable<Bitmap>("printBitmap")
        if (bitmap != null) {
            doPrint(bitmap)
        }
    }

    private fun doPrint(bitmap: Bitmap) {
        // 1. PrintHelper 설정
        val printHelper = PrintHelper(requireContext()).apply {
            // SCALE_MODE_FIT: 이미 구워진 이미지를 용지 안에 딱 맞춰 넣습니다.
            scaleMode = PrintHelper.SCALE_MODE_FIT
            // 세로 출력 강제
            orientation = PrintHelper.ORIENTATION_PORTRAIT
        }

        // 2. 작업 이름 설정
        val jobName = "Feel-em_Card_Print"

        // 3. 인쇄 실행
        // 이미지가 이미 638x1016으로 구워졌으므로 시스템은 이를 카드 사이즈로 인식하기 훨씬 수월해집니다.
        printHelper.printBitmap(jobName, bitmap) {
            // 인쇄 다이얼로그가 닫히면 성공 화면으로 이동
            if (isAdded) {
                findNavController().navigate(R.id.action_loading2Fragment_to_successFragment)
            }
        }
    }

    private fun checkPrintStatus() {
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager

        scope.launch {
            while (isActive) {
                // 현재 앱에서 보낸 가장 최근 인쇄 작업을 찾음
                val jobs = printManager.printJobs
                val currentJob = jobs.find { it.info.label == "Feel-em-Print-Job" }

                if (currentJob != null) {
                    when {
                        currentJob.isCompleted -> {
                            findNavController().navigate(R.id.action_loading2Fragment_to_successFragment)
                            cancel() // 코루틴 종료
                        }
                        currentJob.isFailed -> {
                            val bundle = Bundle().apply { putString("reason", "프린터 연결을 확인해주세요.") }
                            findNavController().navigate(R.id.action_loading2Fragment_to_failFragment, bundle)
                            cancel()
                        }
                        currentJob.isCancelled -> {
                            val bundle = Bundle().apply { putString("reason", "프린트 작업이 중단되었습니다.") }

                            findNavController().navigate(R.id.action_loading2Fragment_to_failFragment)
                            cancel()
                        }
                    }
                }
                delay(1000) // 1초 간격으로 상태 체크
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel() // 프래그먼트 파괴 시 작업 중단
    }
}