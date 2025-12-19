package com.feelem.printkiosk

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.print.PrintJob
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.print.PrintHelper
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.*

class Loading2Fragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var lottieLoading: LottieAnimationView
    private val PRINT_JOB_NAME = "Feel-em_Card_Print"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lottieLoading = view.findViewById(R.id.lottie_loading2)

        // 전달받은 비트맵 확인
        val bitmap = arguments?.getParcelable<Bitmap>("printBitmap")
        if (bitmap != null) {
            doPrint(bitmap)
        } else {
            Log.e("PrintLog", "전달된 비트맵이 없음")
            navigateToFirst()
        }
    }

    private fun doPrint(bitmap: Bitmap) {
        val context = context ?: return
        val printHelper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            orientation = PrintHelper.ORIENTATION_PORTRAIT
        }

        // 인쇄 실행 (시스템 다이얼로그 호출)
        printHelper.printBitmap(PRINT_JOB_NAME, bitmap)

        // 상태 감시 및 타임아웃 로직 시작
        checkPrintStatus()
    }

    private fun checkPrintStatus() {
        val appContext = context?.applicationContext ?: return
        val printManager = appContext.getSystemService(Context.PRINT_SERVICE) as PrintManager

        scope.launch {
            delay(10000) // 사용자가 인쇄 버튼을 누를 때까지 기다림

            var watchTime = 0
            val timeoutLimit = 40000
            var targetJobFound = false // 인쇄 작업이 한 번이라도 생성되었는지 확인하는 플래그

            while (isActive && watchTime < timeoutLimit) {
                val jobs = printManager.printJobs
                val currentJob = jobs.find { it.info.label == PRINT_JOB_NAME }

                if (currentJob != null) {
                    // 작업을 찾음! 이제부터는 '전송 중' 상태로 간주
                    targetJobFound = true

                    val state = currentJob.info.state
                    Log.d("PrintStatus", "현재 상태 코드: $state")

                    when {
                        currentJob.isCompleted -> {
                            Log.d("PrintStatus", "인쇄 완료 감지")
                            navigateToSuccess()
                            return@launch
                        }
                        currentJob.isFailed -> {
                            Log.e("PrintStatus", "인쇄 실패 감지 - 성공 화면으로 우회")
                            navigateToSuccess()
                            return@launch
                        }
                    }
                } else {
                    // 작업이 리스트에 없는 경우
                    if (targetJobFound) {
                        // 1. 작업을 찾았었는데 사라졌다면 -> 인쇄 전송 성공!
                        Log.d("PrintStatus", "작업 완료로 인한 리스트 소멸 -> 성공 처리")
                        navigateToSuccess()
                        return@launch
                    } else {
                        // 2. 처음부터 작업이 없었고, 일정 시간(25초)이 지났다면 -> 사용자 취소로 간주
                        if (watchTime > 25000) {
                            Log.d("PrintStatus", "작업이 생성되지 않음 (사용자 취소 가능성) -> 홈으로")
                            navigateToFirst() // 또는 navigateToFail()
                            return@launch
                        }
                    }
                }

                delay(2000)
                watchTime += 2000
                Log.d("PrintStatus", "감시 중... (${watchTime / 1000}초)")
            }

            // --- 타임아웃 발생 (무한 로딩 등) ---
            Log.e("PrintStatus", "타임아웃 발생! 시스템 큐 강제 정리")
            printManager.printJobs.find { it.info.label == PRINT_JOB_NAME }?.cancel()

            navigateToSuccess()
        }
    }

    private fun navigateToSuccess() {
        if (isAdded) {
            findNavController().navigate(R.id.action_loading2Fragment_to_successFragment)
        }
        scope.cancel()
    }

    private fun navigateToFirst() {
        if (isAdded) {
            findNavController().navigate(R.id.action_loading2Fragment_to_firstFragment)
        }
        scope.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트 파괴 시 코루틴 정리 (메모리 누수 방지)
        scope.cancel()
    }
}