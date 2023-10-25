package com.gmail.golovkobalak.sonar.model

class SonarDataException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    companion object {
        const val FAILED_TO_MEASURE = "failed to measure the depth"
        const val FAILED_TO_UNKNOWN_REASON = "failed to unknown reason"
    }
}