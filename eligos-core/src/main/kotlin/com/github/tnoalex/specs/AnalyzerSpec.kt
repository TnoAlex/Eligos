package com.github.tnoalex.specs

import com.github.tnoalex.foundation.LaunchEnvironment
import java.io.File

data class AnalyzerSpec(
    val majorLang: String,
    val withLang: String?,
    val extendRulePath: File?,
    val enabledDebug: Boolean = false,
    val kotlinCompilerSpec: KotlinCompilerSpec?,
    val formatterSpec: FormatterSpec,
    val launchEnvironment: LaunchEnvironment
)