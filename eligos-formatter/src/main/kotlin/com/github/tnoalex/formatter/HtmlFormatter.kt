package com.github.tnoalex.formatter

import com.github.tnoalex.formatter.utils.encodeHtml
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.specs.FormatterSpec
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.slf4j.LoggerFactory
import java.io.*

class HtmlFormatter : IFormatter {
    override val fileExtension: String
        get() = "html"


    @Suppress("UNCHECKED_CAST")
    override fun format(obj: Any): String {
        val velocityEngine = VelocityEngine()
        velocityEngine.init()
        val templateStream = ApplicationContext.currentClassLoader.getResourceAsStream("${TEMPLATE_PATH}/report.vm")
            ?: InputStream.nullInputStream()
        val stringWriter = StringWriter()
        val encodeObj = encodeHtml(obj)
        val velocityContext = VelocityContext(encodeObj as Map<String, Any>)
        velocityEngine.evaluate(velocityContext, stringWriter, "", BufferedReader(InputStreamReader(templateStream)))
        val res = stringWriter.toString()
        return res
    }

    override fun write(formatted: String, spec: FormatterSpec) {
        val dirName = spec.resultOutPrefix.ifBlank { "result" } + "_" + fileSuffix()
        val filePath = spec.resultOutPath.toFile().path + File.separatorChar + dirName
        logger.info("Write report resources")
        writeCss(filePath)
        writeImg(filePath)
        logger.info("Report will be wrote in $dirName ")
        val html = File(dirName + File.separatorChar + "report." + fileExtension)
        if (!html.exists()){
            if (!html.createNewFile())
                throw RuntimeException("Can not creat html report")
        }
        val fileOutputStream = FileOutputStream(html)
        fileOutputStream.write(formatted.toByteArray(Charsets.UTF_8))
        fileOutputStream.close()
    }

    private fun writeCss(filePath: String) {
        val cssPath = filePath + File.separatorChar + "css"
        writeResources(cssResources, cssPath, CSS_PREFIX)
    }

    private fun writeImg(filePath: String) {
        val imgPath = filePath + File.separatorChar + "img"
        writeResources(imgResources, imgPath, IMG_PREFIX)
    }

    private fun writeResources(resourceNames: List<String>, outResourcePath: String, resourcePrefix: String) {
        val resourceFile = File(outResourcePath)
        if (!resourceFile.exists()) {
            resourceFile.mkdirs()
        }
        resourceNames.forEach {
            val path = "$TEMPLATE_PATH$resourcePrefix/$it"
            val inputStream =
                ApplicationContext.currentClassLoader.getResourceAsStream(path) ?: InputStream.nullInputStream()
            val outputStream = FileOutputStream(File(outResourcePath + File.separatorChar + it))
            val bytes = ByteArray(1024)
            var len = 0
            do {
                len = inputStream.read(bytes)
                if (len != -1) {
                    outputStream.write(bytes, 0, len)
                    outputStream.flush()
                }
            } while (len != -1)
            inputStream.close()
            outputStream.close()
        }
    }

    companion object {
        private val cssResources = listOf("custom.css", "kube.css", "custom.min.css", "kube.min.css")
        private val imgResources = listOf("bg.jpg", "favicon.png")
        private const val TEMPLATE_PATH = "template/html"
        private const val CSS_PREFIX = "/css"
        private const val IMG_PREFIX = "/img"
        private val logger = LoggerFactory.getLogger(HtmlFormatter::class.java)
    }
}