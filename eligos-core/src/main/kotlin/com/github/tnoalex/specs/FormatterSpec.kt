package com.github.tnoalex.specs

import com.github.tnoalex.formatter.FormatterTypeEnum
import java.nio.file.Path

data class FormatterSpec(
    val srcPathPrefix: String,
    val resultOutPath: Path,
    val resultOutPrefix: String,
    val resultFormat: FormatterTypeEnum
)
