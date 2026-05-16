package com.runtracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.data.repository.RunRepository
import com.runtracker.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    runRepo: RunRepository,
    userRepo: UserRepository
) : ViewModel() {

    private val sub = SharingStarted.WhileSubscribed(5_000)

    val username     = userRepo.loggedInUsername.stateIn(viewModelScope, sub, null)
    val recentRuns   = runRepo.getRecent().stateIn(viewModelScope, sub, emptyList())
    val allRuns      = runRepo.getAll().stateIn(viewModelScope, sub, emptyList())
    val totalRuns    = runRepo.countAll().stateIn(viewModelScope, sub, 0)
    val totalDistKm  = runRepo.sumDistance().stateIn(viewModelScope, sub, null)
    val totalCal     = runRepo.sumCalories().stateIn(viewModelScope, sub, null)
    val bestPaceSec  = runRepo.bestPace().stateIn(viewModelScope, sub, null)   // seconds/km
}
