package com.github.tnoalex.formatter

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.specs.FormatterSpec
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.time.ZonedDateTime
import java.util.*

class Reporter(private val formatterSpec: FormatterSpec) {

    private val currentFormatter = getFormatter(formatterSpec.resultFormat)
    fun report() {
        val summary = summary()
        val formatted = format(summary)
        write(formatted)
        logger.info("All done")
    }

    private fun format(report: Any): String {
        logger.info("Formatting")
        return currentFormatter.format(report)
    }

    private fun write(formatted: String) {
        logger.info("Writing")
        val fileName = formatterSpec.resultOutPrefix.ifBlank { "result" } + "_" + fileSuffix()
        val file =
            File(formatterSpec.resultOutPath.toFile().path + File.separatorChar + fileName + "." + currentFormatter.fileExtension)
        if (!file.exists()) {
            if (!file.createNewFile())
                throw RuntimeException("Can not create report")
        }
        logger.info("Result will be wrote in ${file.absolutePath}")
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(formatted.toByteArray(Charsets.UTF_8))
        fileOutputStream.close()
    }

    private fun fileSuffix(): String {
        return BigInteger.valueOf(System.currentTimeMillis() / 1000).toString(16)
    }

    private fun summary(): HashMap<String, out Any> {
        logger.info("Build summary")
        val context = ApplicationContext.getExactBean(Context::class.java)!!
        val summary = LinkedHashMap<String, Any>()
        summary["MetaInfo"] = getMetaInfo()
        val statistics = LinkedHashMap<String, HashMap<String, out Any>>()
        context.stats.forEach {
            statistics[it::class.simpleName.toString()] = it.unwrap(formatterSpec)
        }
        val issues = LinkedHashMap<String, ArrayList<Map<out Any, Any>>>()
        context.issues.forEach {
            issues.getOrPut(it.issueName) { ArrayList() }.add(it.unwrap(formatterSpec))
        }
        val issueStatistics = HashMap<String, Int>()
        issues.forEach { (k, v) ->
            issueStatistics[k] = v.size
        }
        statistics["IssueStatistics"] = issueStatistics
        summary["Statistics"] = statistics
        summary["Issues"] = issues
        return summary
    }

    private fun getMetaInfo(): LinkedHashMap<String, Any> {
        val stream = ApplicationContext.currentClassLoader.getResourceAsStream("eligos-meta.properties") ?: InputStream.nullInputStream()
        val properties = Properties()
        properties.load(stream)
        val metaInfo = LinkedHashMap<String, Any>()
        properties.toMap().forEach { (k, v) ->
            metaInfo[k.toString()] = v
        }
        metaInfo["Create Time"] = ZonedDateTime.now().toString()
        metaInfo["Source Base Path"] = formatterSpec.srcPathPrefix
        stream.close()
        return metaInfo
    }

    companion object {
        fun getFormatter(type: FormatterTypeEnum): IFormatter {
            return when (type) {
                FormatterTypeEnum.JSON -> JsonFormatter()
                FormatterTypeEnum.XML -> XmlFormatter()
                FormatterTypeEnum.HTML -> HtmlFormatter()
                FormatterTypeEnum.TEXT -> TextFormatter()
                else -> throw RuntimeException("Can not found formatter with type $type")
            }
        }

        @JvmStatic
        private val logger = LoggerFactory.getLogger(Reporter::class.java)
    }
}