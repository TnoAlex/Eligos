package com.github.tnoalex.analyzer

import depends.extractor.AbstractLangProcessor
import org.antlr.v4.runtime.tree.ParseTreeListener
import java.util.*

abstract class AbstractAnalyzerContext {
    protected fun setupLangProcessor() {

    }

    companion object {
        private val processorMap = HashMap<AbstractLangProcessor, LinkedList<ParseTreeListener>>()
    }
}