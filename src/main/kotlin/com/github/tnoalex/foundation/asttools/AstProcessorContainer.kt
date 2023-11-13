package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.utils.loadServices
import java.util.*

object AstProcessorContainer {
    private val processors = HashMap<String, LinkedList<AstProcessor>>()

    init {
        loadServices(AstProcessor::class.java).forEach {
            it.hookAst()
            addProcess(it.supportLanguage, it)
        }
    }

    fun addProcess(supportLang: String, processor: AstProcessor) {
        if (processors[supportLang] == null) {
            processors[supportLang] = LinkedList(listOf(processor))
        } else {
            processors[supportLang]!!.add(processor)
        }
    }

    fun getProcessors() = processors.values.toList()

    fun <T : AstProcessor> getProcessByType(type: Class<T>): AstProcessor? {
        processors.values.forEach {
            val res = it.filter { p ->
                p.javaClass == type
            }
            if (res.isNotEmpty()) {
                return res[0]
            }
        }
        return null
    }

    fun getProcessByLang(lang: String): LinkedList<AstProcessor>? {
        return processors[lang]
    }

    fun hookAstByLang(lang: String) {
        processors[lang]?.forEach {
            it.hookAst()
        }
    }

    fun hookAllProcessor() {
        processors.values.forEach {
            it.forEach { p ->
                p.hookAst()
            }
        }
    }

    fun removeHooksByLang(lang: String) {
        processors[lang]?.forEach {
            it.removeHooks()
        }
    }

    fun removeAllHooks() {
        processors.values.forEach {
            it.forEach { p ->
                p.removeHooks()
            }
        }
    }
}