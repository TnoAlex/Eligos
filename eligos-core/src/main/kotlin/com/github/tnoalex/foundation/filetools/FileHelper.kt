package com.github.tnoalex.foundation.filetools

import com.github.tnoalex.foundation.bean.Component
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

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
        Files.walkFileTree(sourceFilePath.toPath(), object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                hook(file.toFile())
                return super.visitFile(file, attrs)
            }
        })
    }
}