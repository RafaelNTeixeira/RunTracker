package com.runtracker.ui.screens.logrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.data.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogRunViewModel @Inject constructor(private val repo: RunRepository) : ViewModel() {

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun saveRun(title: String, distanceKm: Double, durationSeconds: Int, calories: Int, notes: String, runDate: String) {
        viewModelScope.launch {
            repo.add(RunEntity(title = title, distanceKm = distanceKm, durationSeconds = durationSeconds,
                calories = calories, notes = notes, runDate = runDate))
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}
