package com.gmail.golovkobalak.sonar.service

import kotlinx.coroutines.delay

// Simulated heavy logic with progress callback
suspend fun heavyLogicSimulation(progressCallback: (Float) -> Unit) {
    // Replace this with your actual heavy logic or computation
    val totalIterations = 100 // Number of iterations for the simulation
    for (i in 0..totalIterations) {
        // Simulate heavy computation here
        // Update the progress value (between 0.0 and 1.0) based on the current iteration
        val progressValue = i.toFloat() / totalIterations.toFloat()
        // Invoke the progress callback to update the progress bar in the UI
        progressCallback(progressValue)
        // Simulate some delay between iterations
        delay(50)
    }
}