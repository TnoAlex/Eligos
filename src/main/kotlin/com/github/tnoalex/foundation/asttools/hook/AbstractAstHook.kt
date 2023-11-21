package com.github.tnoalex.foundation.asttools.hook

import com.github.tnoalex.foundation.asttools.AstProcessor
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

abstract class AbstractAstHook {
    private val hookMap = HashMap<String, LinkedList<(ParserRuleContext) -> Unit>>()
    private val processorMap = HashMap<AstProcessor, LinkedList<(ParserRuleContext) -> Unit>>()
    protected fun addHook(hookPoint: String, hook: (ParserRuleContext) -> Unit, processor: AstProcessor) {
        if (hookMap[hookPoint] == null) {
            hookMap[hookPoint.lowercase()] = LinkedList(listOf(hook))
        } else {
            hookMap[hookPoint.lowercase()]!!.add(hook)
        }

        if (processorMap[processor] == null) {
            processorMap[processor] = LinkedList(listOf(hook))
        } else {
            processorMap[processor]!!.add(hook)
        }
    }

    fun getHook(hookPoint: String): LinkedList<(ParserRuleContext) -> Unit> {
        return hookMap[hookPoint.lowercase()] ?: LinkedList()
    }

    fun removeHook(processor: AstProcessor) {
        processorMap[processor]?.forEach {
            hookMap.values.forEach { h ->
                if (h.contains(it)) {
                    h.remove(it)
                }
            }
        }
        processorMap.remove(processor)
    }
}