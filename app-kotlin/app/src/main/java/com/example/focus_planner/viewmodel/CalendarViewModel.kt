package com.example.focus_planner.viewmodel

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_planner.data.model.task.Task
import com.example.focus_planner.data.model.task.TaskSummaryDTO
import com.example.focus_planner.data.repository.TaskRepository
import com.example.focus_planner.network.ApiService
import com.example.focus_planner.ui.screens.calendar.CalendarTask
import com.example.focus_planner.utils.SharedPreferencesManager.getToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel  @Inject constructor(private val repository: TaskRepository) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth

    private val _tasks = MutableStateFlow<List<CalendarTask>>(emptyList())
    val tasks: StateFlow<List<CalendarTask>> = _tasks

//    val context = LocalContext.current


    fun cargarToken(token :String) {
        loadTasksForMonth(_currentYearMonth.value, token)
    }

    fun loadTasksForMonth(yearMonth: YearMonth, token: String) {
        viewModelScope.launch {
            val startDate = yearMonth.atDay(1)  // "YYYY-MM-DD"
            val endDate = yearMonth.atEndOfMonth()
            val fullTasks: List<Task> = repository.getTasksByDateRange(startDate, endDate, token)

            // Mapear de Task (completo) a TaskSummaryDTO (solo título y fecha)
            val taskSummaries = fullTasks.map {
                it.dueDate?.let { it1 ->
                    TaskSummaryDTO(
                        title = it.title,
                        dueDate = it1
                    )
                }
            }

            // Mapear de TaskSummaryDTO a CalendarTask con LocalDate parseado
            val calendarTasks = taskSummaries.mapNotNull { summary ->
                val date = summary?.dueDate?.let { LocalDate.parse(it) }
                if (date != null) {
                    CalendarTask(date, summary.title)
                } else {
                    null
                }
            }

            _tasks.value = calendarTasks
        }
    }

    fun onMonthChanged(newMonth: YearMonth, token: String) {
        _currentYearMonth.value = newMonth
        loadTasksForMonth(newMonth,token)
    }
}
