package com.github.tnoalex.formatter

import com.github.tnoalex.specs.FormatterSpec
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger

interface IFormatter {
    val fileExtension: String
    fun format(obj: Any): String
    fun write(formatted: String, spec: FormatterSpec) {
        logger.info("Writing")
        val fileName = spec.resultOutPrefix.ifBlank { "result" } + "_" + fileSuffix()
        val file =
            File(spec.resultOutPath.toFile().path + File.separatorChar + fileName + "." + fileExtension)
        if (!file.exists()) {
            if (!file.createNewFile())
                throw RuntimeException("Can not create report")
        }
        logger.info("Result will be wrote in ${file.absolutePath}")
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(formatted.toByteArray(Charsets.UTF_8))
        fileOutputStream.close()
    }

    fun fileSuffix(): String {
        return BigInteger.valueOf(System.currentTimeMillis() / 1000).toString(16)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IFormatter::class.java)
    }
}