package com.gmail.golovkobalak.sonar.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object CacheProgress {
    private val progressFlow = MutableStateFlow(0f)
    private val isLoadingFlow = MutableStateFlow(false)
    var tilesTotal = 1;

    fun getProgressFlow(): Flow<Float> = progressFlow

    fun updateProgress(progress: Float) {
        progressFlow.value = progress
    }

    fun isLoadingFlow(): Flow<Boolean> = isLoadingFlow

    fun updateLoading(isLoading: Boolean) {
        isLoadingFlow.value = isLoading
    }
}