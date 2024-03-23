package com.github.tnoalex.formatter

import com.github.tnoalex.Context
import com.github.tnoalex.formatter.utils.getMetaInfoProperties
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.specs.FormatterSpec
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class Reporter(private val formatterSpec: FormatterSpec) {

    private val currentFormatter = getFormatter(formatterSpec.resultFormat)
    fun report() {
        val summary = summary()
        val formatted = format(summary)
        currentFormatter.write(formatted, formatterSpec)
        logger.info("All done")
    }

    private fun format(report: Any): String {
        logger.info("Formatting")
        return currentFormatter.format(report)
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
        val metaInfo = LinkedHashMap<String, Any>()
        getMetaInfoProperties()?.let { metaInfo.putAll(it) }
        metaInfo["Create Time"] = ZonedDateTime.now().toString()
        metaInfo["Source Base Path"] = formatterSpec.srcPathPrefix
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