package com.runtracker.ui.screens.motivation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.data.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class Quote(val text: String, val author: String)

@HiltViewModel
class MotivationViewModel @Inject constructor(runRepo: RunRepository) : ViewModel() {

    private val quotes = listOf(
        Quote("The miracle isn't that I finished. The miracle is that I had the courage to start.", "John Bingham"),
        Quote("Pain is temporary. Quitting lasts forever.", "Lance Armstrong"),
        Quote("If you run, you are a runner. It doesn't matter how fast or how far.", "John Bingham"),
        Quote("The will to win means nothing without the will to prepare.", "Juma Ikangaa"),
        Quote("Running is the greatest metaphor for life — you get out of it what you put into it.", "Oprah Winfrey"),
        Quote("Ask yourself: 'Can I give more?' The answer is usually: 'Yes'.", "Paul Tergat"),
        Quote("Most people never run far enough on their first wind to find out they've got a second.", "William James"),
        Quote("Champions are made from something deep inside them — a desire, a dream, a vision.", "Muhammad Ali"),
        Quote("Run when you can, walk if you have to, crawl if you must — just never give up.", "Dean Karnazes"),
        Quote("No matter how slow you go, you are still lapping everyone on the couch.", "Unknown"),
    )

    private val tips = listOf(
        "Warm up with 5 minutes of brisk walking before each run.",
        "Follow the 10% rule: never increase weekly mileage by more than 10%.",
        "Aim for 170–180 steps per minute for optimal cadence.",
        "Breathe through both nose and mouth to maximise oxygen intake.",
        "Run easy days easy — conversational pace builds aerobic base.",
        "Strength training twice a week improves running economy.",
        "Stay hydrated: drink 500ml of water 2 hours before a long run.",
        "Include one rest day per week to prevent injury.",
        "Replace shoes every 500–800 km.",
        "Negative splits (faster second half) are a sign of great pacing.",
    )

    private val _quoteIdx = MutableStateFlow(0)
    val quote = _quoteIdx.map { quotes[it % quotes.size] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), quotes[0])

    val tip = _quoteIdx.map { tips[it % tips.size] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), tips[0])

    val allRuns = runRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val streak = allRuns.map { calcStreak(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun nextQuote() { _quoteIdx.value++ }

    private fun calcStreak(runs: List<RunEntity>): Int {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dates = runs.map { it.runDate }.toSet()
        var streak = 0
        var day = LocalDate.now()
        for (i in 0..365) {
            if (dates.contains(day.format(fmt))) streak++ else if (i > 0) break
            day = day.minusDays(1)
        }
        return streak
    }
}
