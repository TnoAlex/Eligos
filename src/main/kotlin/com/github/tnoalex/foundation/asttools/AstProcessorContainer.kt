package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.utils.loadServices

object AstProcessorContainer {
    private val processors = HashSet<AstProcessor>()

    init {
        loadServices(AstProcessor::class.java).forEach {
            it.hookAst()
            addProcess(it)
        }
    }

    fun addProcess(processor: AstProcessor) {
        processors.add(processor)
    }

    fun getProcessors() = processors.toList()

    fun <T : AstProcessor> getProcessByType(type: Class<T>): AstProcessor? {
        return processors.firstOrNull {
            it::class.java == type
        }
    }
}