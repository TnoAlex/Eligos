package com.github.tnoalex.formatter

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.specs.FormatterSpec
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
        val fileName = formatterSpec.resultOutPrefix.ifBlank { "result" } + "_" + fileSuffix(formatted)
        val file =
            File(formatterSpec.resultOutPath.toFile().path + File.separatorChar + fileName + "." + currentFormatter.fileExtension)
        if (!file.exists()) {
            if (!file.createNewFile())
                throw RuntimeException("Can not create report")
        }
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(formatted.toByteArray())
        fileOutputStream.close()
    }

    private fun fileSuffix(file: String): String {
        var suffix = ""
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(file.toByteArray())
            suffix = BigInteger(1, md5.digest()).toString(16).substring(0..7)
        } catch (e: NoSuchAlgorithmException) {
            logger.warn("Can not compute report suffix")
        }
        return suffix
    }

    private fun summary(): HashMap<String, out Any> {
        logger.info("Build summary")
        val context = ApplicationContext.getExactBean(Context::class.java)!!
        val summary = LinkedHashMap<String, Any>()
        val statistics = HashMap<String, HashMap<String, out Any>>()
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

    companion object {
        fun getFormatter(type: FormatterTypeEnum): IFormatter {
            if (type == FormatterTypeEnum.JSON) {
                return JsonFormatter()
            }
            throw RuntimeException("Can not found formatter with type $type")
        }

        @JvmStatic
        private val logger = LoggerFactory.getLogger(Reporter::class.java)
    }
}