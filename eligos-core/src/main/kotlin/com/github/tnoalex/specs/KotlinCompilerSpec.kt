package com.github.tnoalex.specs

import java.nio.file.Path

data class KotlinCompilerSpec(
    val srcPath: Path,
    val classPath: List<Path>,
    val jdkHome: Path,
    val kotlinVersion: String = "1.9",
    val jvmTarget: String = "1.8",
    val kotlinStdLibPath: Path
)

