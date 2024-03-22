package com.github.tnoalex.specs

data class DebugSpec(
    val enabledDebug: Boolean = false,
    val nonReport: Boolean = false,
    val disableAnyElse: List<String> = emptyList(),
    val enableAnyElse: List<String> = emptyList()
) {
    init {
        require(disableAnyElse.isEmpty() || enableAnyElse.isEmpty())
    }

    fun notAllowedReport() = enabledDebug && nonReport
}