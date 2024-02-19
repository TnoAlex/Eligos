package com.github.tnoalex.foundation.filetools

import com.github.tnoalex.foundation.bean.Component
import java.io.File

@Component(order = Int.MAX_VALUE)
class FileHelper {
    lateinit var sourceFilePath: File
        private set

    lateinit var outputPath: File
        private set

    var outputFilePrefix: String = "analysis_result"
        private set

    fun setFileInfo(sourcePath: File, outPath: File?, prefix: String?) {
        sourceFilePath = sourcePath
        outputPath = outPath ?: File(sourcePath.path + File.pathSeparator + "out")
        outputFilePrefix = prefix ?: outputFilePrefix
    }

    fun visitSourcesFile(hook: (File) -> Unit) {
        fun visit(file: File) {
            if (file.isDirectory()) {
                val files = file.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.isDirectory()) {
                            visit(f)
                        } else {
                            hook(f)
                        }
                    }
                }
            }
        }
        visit(sourceFilePath)
    }
}