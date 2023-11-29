package com.github.tnoalex.processor

import com.github.tnoalex.Context

abstract class ContextAstProcessor : AstProcessor {
    protected lateinit var context: Context

    override fun initContext(context: Context) {
        this.context = context
    }
}