package com.example.focus_planner.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.PomodoroUiState
import com.example.focus_planner.ui.notificaciones.showPomodoroNotification
import com.example.focus_planner.ui.notificaciones.updatePomodoroProgressNotification
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_BREAK_TIME
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_END_TIME
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_IS_RUNNING
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_IS_WORK_TIME
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_TIME_LEFT
import com.example.focus_planner.utils.SharedPreferencesManager.KEY_WORK_TIME
import com.example.focus_planner.utils.SharedPreferencesManager.PREFS_NAME
import com.example.focus_planner.utils.SharedPreferencesManager.loadPomodoroState
import com.example.focus_planner.utils.SharedPreferencesManager.savePomodoroState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val appContext: Application
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("focus_planner_prefs", Context.MODE_PRIVATE)

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _workTime = MutableStateFlow(25)
    val workTime: StateFlow<Int> = _workTime

    private val _breakTime = MutableStateFlow(5)
    val breakTime: StateFlow<Int> = _breakTime

    private val _isWorkTime = MutableStateFlow(true)
    val isWorkTime: StateFlow<Boolean> = _isWorkTime

    init {
        val state = loadPomodoroState(appContext)
        _isTimerRunning.value = state.isRunning
        _timeLeft.value = state.timeLeft
        _workTime.value = state.workTime
        _breakTime.value = state.breakTime
        _isWorkTime.value = state.isWorkTime

        observeSharedPreferences()
        startTimerLoop()
    }

    private fun observeSharedPreferences() {
        viewModelScope.launch {
            snapshotFlow { prefs.getBoolean("isRunning", _isTimerRunning.value) }
                .distinctUntilChanged()
                .collect { running ->
                    _isTimerRunning.value = running
                }
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_isTimerRunning.value && _timeLeft.value > 0) {
                    _timeLeft.value = _timeLeft.value - 1

                    savePomodoroState(appContext, _isTimerRunning.value, _timeLeft.value, _workTime.value, _breakTime.value, _isWorkTime.value)

                    updatePomodoroProgressNotification(
                        context = appContext,
                        timeLeft = _timeLeft.value,
                        totalTime = if (_isWorkTime.value) _workTime.value * 60 else _breakTime.value * 60,
                        isRunning = _isTimerRunning.value
                    )

                    if (_timeLeft.value <= 0) {
                        val wasWorkTime = _isWorkTime.value

                        _isTimerRunning.value = false
                        _isWorkTime.value = !_isWorkTime.value
                        _timeLeft.value = if (_isWorkTime.value) _workTime.value * 60 else _breakTime.value * 60

                        showPomodoroNotification(
                            appContext,
                            if (!wasWorkTime) "¡Tiempo de trabajar!" else "¡Hora de deescansar!",
                            if (!wasWorkTime) "Vamos con otra ronda de trabajo." else "Tómate un merecido descanso."
                        )

                        savePomodoroState(
                            appContext,
                            _isTimerRunning.value,
                            _timeLeft.value,
                            _workTime.value,
                            _breakTime.value,
                            _isWorkTime.value
                        )
                    }
                }
            }
        }
    }

    fun toggleTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
        prefs.edit().putBoolean("isRunning", _isTimerRunning.value).apply()
        savePomodoroState(appContext, _isTimerRunning.value, _timeLeft.value, _workTime.value, _breakTime.value, _isWorkTime.value)
    }

    fun setWorkAndBreakTimes(work: Int, breakT: Int) {
        _workTime.value = work
        _breakTime.value = breakT
        _timeLeft.value = if (_isWorkTime.value) work * 60 else breakT * 60
        _isTimerRunning.value = false
        prefs.edit().putBoolean("isRunning", false).apply()
        savePomodoroState(appContext, _isTimerRunning.value, _timeLeft.value, _workTime.value, _breakTime.value, _isWorkTime.value)
    }
    fun pauseTimer() {
        _isTimerRunning.value = false
        savePomodoroState(appContext, _isTimerRunning.value, _timeLeft.value, _workTime.value, _breakTime.value, _isWorkTime.value)
    }

    fun resumeTimer() {
        _isTimerRunning.value = true
        savePomodoroState(appContext, _isTimerRunning.value, _timeLeft.value, _workTime.value, _breakTime.value, _isWorkTime.value)
    }

}



