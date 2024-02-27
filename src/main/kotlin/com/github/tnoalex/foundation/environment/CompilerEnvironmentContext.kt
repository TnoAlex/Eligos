package com.github.tnoalex.foundation.environment

import com.github.tnoalex.foundation.LanguageSupportInfo
import java.nio.file.Path

interface CompilerEnvironmentContext : LanguageSupportInfo {
    fun initCompilerEnv(filePath: Path)
}