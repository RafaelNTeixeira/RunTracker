package com.runtracker.ui.screens.myruns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.data.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRunsViewModel @Inject constructor(private val repo: RunRepository) : ViewModel() {

    val runs = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(run: RunEntity) = viewModelScope.launch { repo.delete(run) }

    fun update(run: RunEntity) = viewModelScope.launch { repo.update(run) }
}
