package com.example.focus_planner.ui.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.focus_planner.viewmodel.PomodoroViewModel


class PomodoroActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return

        val action = intent?.action
        if (action == "ACTION_PAUSE_TIMER") {
            PomodoroStateHolder.pause()
        } else if (action == "ACTION_RESUME_TIMER") {
            PomodoroStateHolder.resume()
        }
    }
}

object PomodoroStateHolder {
    lateinit var viewModel: PomodoroViewModel

    fun bind(viewModel: PomodoroViewModel) {
        this.viewModel = viewModel
    }

    fun pause() {
        if (::viewModel.isInitialized) {
            viewModel.pauseTimer()
        }
    }

    fun resume() {
        if (::viewModel.isInitialized) {
            viewModel.resumeTimer()
        }
    }
}