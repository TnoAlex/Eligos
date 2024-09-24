package com.github.tnoalex.issues

enum class ConfidenceLevel {
    EXTREMELY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    COMPLETELY_TRUSTWORTHY;
    companion object {
        @JvmStatic
        val DEFAULT = LOW
    }
}