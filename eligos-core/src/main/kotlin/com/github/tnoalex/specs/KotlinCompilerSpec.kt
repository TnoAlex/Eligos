package com.github.tnoalex.specs

import java.nio.file.Path

data class KotlinCompilerSpec(
    val srcPath: Path,
    val classpath: List<Path>,
    val jdkHome: Path,
    val apiVersion: String = "2.3",
    val languageVersion: String = "2.3",
    val jvmTarget: String = "1.8",
    val kotlinStdLibPath: Path,
    val disableCompilerLog: Boolean,
    val freeCompilerArgs: List<String> = emptyList(),
)

