package com.github.tnoalex.foundation.filetools

import java.io.File

object FileContainer {
    var sourceFilePath: File? = null
        private set

    var outputPath: File? = null
        private set

    var outputFilePrefix: String = "analysis_result"
        private set

    fun initFileContainer(sourcePath: File, outPath: File?, prefix: String?) {
        sourceFilePath = sourcePath
        outputPath = outPath ?: File(sourcePath.path + File.pathSeparator + "out")
        outputFilePrefix = prefix ?: outputFilePrefix
    }
}