package com.example.focus_planner.ui.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.focus_planner.MainActivity
import com.example.focus_planner.R
import com.example.focus_planner.utils.SharedPreferencesManager
import com.example.focus_planner.utils.SharedPreferencesManager.savePomodoroState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun showPomodoroNotification(context: Context, title: String, message: String) {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("navigate_to", "pomodoro")
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, "pomodoro_channel")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(pendingIntent) // <- Aquí se lanza la app al tocar la notificación
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    NotificationManagerCompat.from(context).notify(1, builder.build())
}



fun updatePomodoroProgressNotification(
    context: Context,
    timeLeft: Int,
    totalTime: Int,
    isRunning: Boolean
) {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

    val progress = totalTime - timeLeft

    val actionIntent = Intent(context, PomodoroActionReceiver::class.java).apply {
        action = if (isRunning) "ACTION_PAUSE_TIMER" else "ACTION_RESUME_TIMER"
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        actionIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val actionText = if (isRunning) "Pausar" else "Reanudar"

    val builder = NotificationCompat.Builder(context, "pomodoro_channel")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Temporizador Pomodoro")
        .setContentText("Tiempo restante: ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}")
        .setProgress(totalTime, progress, false)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOnlyAlertOnce(true)

//        .addAction(R.drawable.pause, actionText, pendingIntent)

    NotificationManagerCompat.from(context).notify(1, builder.build())
}

//object PomodoroTimerManager {
//
//    private var countDownTimer: CountDownTimer? = null
//    private var context: Context? = null
//    private var isRunningInternal = false
//    private var timeLeftInternal = 0
//    private var totalTimeInternal = 0
//    private var isWorkTimeInternal = true
//
//    private val _timeLeftFlow = MutableStateFlow(0)
//    val timeLeftFlow: StateFlow<Int> get() = _timeLeftFlow
//
//    private val _isRunningFlow = MutableStateFlow(false)
//    val isRunningFlow: StateFlow<Boolean> get() = _isRunningFlow
//
//    fun init(context: Context) {
//        this.context = context.applicationContext
//    }
//
//    fun start(
//        workTimeSeconds: Int,
//        breakTimeSeconds: Int,
//        isWorkTime: Boolean,
//        timeLeftSeconds: Int? = null
//    ) {
//        if (context == null) return
//
//        isWorkTimeInternal = isWorkTime
//        totalTimeInternal = if (isWorkTime) workTimeSeconds else breakTimeSeconds
//        timeLeftInternal = timeLeftSeconds ?: totalTimeInternal
//
//        isRunningInternal = true
//        _isRunningFlow.value = true
//        _timeLeftFlow.value = timeLeftInternal
//
//        countDownTimer?.cancel()
//        countDownTimer = object : CountDownTimer(timeLeftInternal * 1000L, 1000L) {
//            override fun onTick(millisUntilFinished: Long) {
//                timeLeftInternal = (millisUntilFinished / 1000L).toInt()
//                _timeLeftFlow.value = timeLeftInternal
//                updatePomodoroProgressNotification(
//                    context = context!!,
//                    timeLeft = timeLeftInternal,
//                    totalTime = totalTimeInternal,
//                    isRunning = true
//                )
//                saveState()
//            }
//
//            override fun onFinish() {
//                isRunningInternal = false
//                _isRunningFlow.value = false
//                showPomodoroNotification(
//                    context!!,
//                    title = if (isWorkTimeInternal) "¡Descanso terminado!" else "¡Tiempo de trabajar!",
//                    message = if (isWorkTimeInternal) "Vamos con otra ronda de trabajo." else "Tómate un merecido descanso."
//                )
//                saveState()
//            }
//        }.start()
//        saveState()
//    }
//
//    fun pause() {
//        countDownTimer?.cancel()
//        isRunningInternal = false
//        _isRunningFlow.value = false
//        context?.let {
//            updatePomodoroProgressNotification(it, timeLeftInternal, totalTimeInternal, false)
//            saveState()
//        }
//    }
//
//    fun resume() {
//        if (isRunningInternal) return
//        if (timeLeftInternal > 0) {
//            start(
//                workTimeSeconds = if (isWorkTimeInternal) totalTimeInternal else 0,
//                breakTimeSeconds = if (!isWorkTimeInternal) totalTimeInternal else 0,
//                isWorkTime = isWorkTimeInternal,
//                timeLeftSeconds = timeLeftInternal
//            )
//        }
//    }
//
//    fun toggleRunning() {
//        if (isRunningInternal) pause() else resume()
//    }
//
//    fun getState(): SharedPreferencesManager.PomodoroState {
//        return SharedPreferencesManager.PomodoroState(
//            isRunning = isRunningInternal,
//            timeLeft = timeLeftInternal,
//            workTime = if (isWorkTimeInternal) totalTimeInternal / 60 else 25,
//            breakTime = if (!isWorkTimeInternal) totalTimeInternal / 60 else 5,
//            isWorkTime = isWorkTimeInternal
//        )
//    }
//
//    private fun saveState() {
//        context?.let {
//            savePomodoroState(
//                it,
//                isRunningInternal,
//                timeLeftInternal,
//                if (isWorkTimeInternal) totalTimeInternal / 60 else 25,
//                if (!isWorkTimeInternal) totalTimeInternal / 60 else 5,
//                isWorkTimeInternal
//            )
//        }
//    }
//}

