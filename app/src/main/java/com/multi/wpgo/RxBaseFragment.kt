package com.multi.wpgo

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job

open class RxBaseFragment : Fragment() {

    protected var subscriptions = CompositeDisposable()
    protected var job: Job? = null // (1) Job 변수 선언

    override fun onResume() {
        super.onResume()
        job = null // (2) 재개 될 때 코루틴 제거
    }

    override fun onPause() {
        super.onPause()
        job?.cancel() // (3) 코루틴의 취소 및 제거
        job = null
    }
}