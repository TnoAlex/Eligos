package com.github.tnoalex.foundation.astprocessor

import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

abstract class AbstractAstHook {
    private val hookMap = HashMap<String, LinkedList<(ParserRuleContext) -> Unit>>()

    protected fun addHook(hookPoint: String, hook: (ParserRuleContext) -> Unit) {
        if (hookMap[hookPoint] == null) {
            hookMap[hookPoint.lowercase()] = LinkedList(listOf(hook))
        } else {
            hookMap[hookPoint.lowercase()]!!.add(hook)
        }
    }

    fun getHook(hookPoint: String): LinkedList<(ParserRuleContext) -> Unit> {
        return hookMap[hookPoint.lowercase()] ?: LinkedList()
    }
}