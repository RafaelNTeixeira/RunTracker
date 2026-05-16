package com.runtracker.ui.screens.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.db.entity.RouteEntity
import com.runtracker.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(private val repo: RouteRepository) : ViewModel() {

    val routes = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveRoute(name: String, description: String, distanceKm: Double, waypoints: String) {
        viewModelScope.launch {
            repo.add(RouteEntity(name = name, description = description, distanceKm = distanceKm, waypoints = waypoints))
        }
    }

    fun delete(route: RouteEntity) = viewModelScope.launch { repo.delete(route) }
}
