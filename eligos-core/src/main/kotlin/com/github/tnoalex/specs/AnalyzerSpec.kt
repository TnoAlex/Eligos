package com.github.tnoalex.specs

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.issues.Severity
import java.io.File

data class AnalyzerSpec(
    val majorLang: String,
    val withLang: String? = null,
    val extendRulePath: File? = null,
    val severityLevel: Severity = Severity.SUGGESTION,
    val kotlinCompilerSpec: KotlinCompilerSpec?,
    val formatterSpec: FormatterSpec,
    val launchEnvironment: LaunchEnvironment,
    val debugSpec: DebugSpec,
    val exceptionHandler: ((Thread, Throwable) -> Unit)? = null
)