package com.github.tnoalex.foundation.environment

import java.nio.file.Path

interface CompilerEnvironmentContext {
    fun initCompilerEnv(filePath: Path)
}