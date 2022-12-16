package com.censocustody.android.common

import android.os.CountDownTimer

interface CensoCountDownTimer {
    fun startCountDownTimer(countdownInterval: Long, onTickCallback: () -> Unit)
    fun stopCountDownTimer()
}

class CensoCountDownTimerImpl : CensoCountDownTimer {
    private var timer: CountDownTimer? = null

    override fun startCountDownTimer(countdownInterval: Long, onTickCallback: () -> Unit) {
        timer = object : CountDownTimer(Long.MAX_VALUE, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                onTickCallback()
            }

            override fun onFinish() {}
        }
        timer?.start()
    }

    override fun stopCountDownTimer() {
        timer?.cancel()
    }

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}