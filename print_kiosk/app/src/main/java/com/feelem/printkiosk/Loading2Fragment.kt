package com.feelem.printkiosk

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.print.PrintJob
import android.print.PrintManager
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

        val bitmap = arguments?.getParcelable<Bitmap>("printBitmap")
        if (bitmap != null) {
            doPrint(bitmap)
        } else {
            // 비트맵이 없으면 처음으로
            findNavController().navigate(R.id.action_loading2Fragment_to_firstFragment)
        }
    }

    private fun doPrint(bitmap: Bitmap) {
        val printHelper = PrintHelper(requireContext()).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            orientation = PrintHelper.ORIENTATION_PORTRAIT
        }

        // 인쇄 실행
        printHelper.printBitmap(PRINT_JOB_NAME, bitmap) {
            android.util.Log.d("PrintLog", "인쇄 다이얼로그 상호작용 끝")
        }

        // 상태 감시 시작
        checkPrintStatus()
    }

    private fun checkPrintStatus() {
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager

        scope.launch {
            // [중요] 사용자가 '인쇄'를 누를 때까지 충분히 기다림 (다이얼로그 열려있는 시간 고려)
            delay(3000)

            var targetJobId: android.print.PrintJobId? = null
            var watchTime = 0
            val maxWatchTime = 90000 // 1분 30초 -> 강제 성공 처리

            while (isActive && watchTime < maxWatchTime) {
                val jobs = printManager.printJobs

                // 1. 타겟 작업 찾기 (이름으로 찾기)
                val currentJob = jobs.find { it.info.label == PRINT_JOB_NAME }

                if (currentJob != null) {
                    targetJobId = currentJob.id // ID 저장

                    when {
                        currentJob.isCompleted -> {
                            android.util.Log.d("PrintStatus", "완료 상태 감지")
                            delay(1000)
                            navigateToSuccess()
                            return@launch
                        }
                        currentJob.isFailed -> {
                            navigateToFail("인쇄 실패")
                            return@launch
                        }
                        currentJob.isCancelled -> {
                            navigateToFirst()
                            return@launch
                        }
                    }
                } else {
                    // 2. 작업이 리스트에 없는데 ID를 한 번이라도 잡았었다면 -> 전송 완료되어 리스트에서 나간 것
                    if (targetJobId != null) {
                        android.util.Log.d("PrintStatus", "작업이 리스트에서 사라짐 -> 전송 완료로 간주")
                        delay(2000)
                        navigateToSuccess()
                        return@launch
                    }
                }

                delay(1500)
                watchTime += 1500
                android.util.Log.d("PrintStatus", "감시 중... (${watchTime/1000}초)")
            }

            // 3. 타임아웃 (30초가 지났는데도 상태가 안 변하면 강제로 성공 처리)
            android.util.Log.d("PrintStatus", "타임아웃 발생 -> 강제 화면 전환")
            navigateToSuccess()
        }
    }

    private fun navigateToSuccess() {
        if (isAdded) {
            findNavController().navigate(R.id.action_loading2Fragment_to_successFragment)
        }
        scope.cancel()
    }

    private fun navigateToFail(reason: String) {
        if (isAdded) {
            val bundle = Bundle().apply { putString("reason", reason) }
            findNavController().navigate(R.id.action_loading2Fragment_to_failFragment, bundle)
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
        scope.cancel()
    }
}