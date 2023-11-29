package com.github.tnoalex.processor

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.common.LanguageSupportInfo
import com.github.tnoalex.foundation.eventbus.EventBus

interface AstProcessor : LanguageSupportInfo {
    /**
     * This property is used to identify the order in which the processor registers into the event bus
     * (affecting the order in which it is called).
     * when it is greater than [Short.MAX_VALUE],it will be called during the AST traversal phase,
     * when it is less than [Short.MAX_VALUE] and greater than 0,it will be called at the end of the AST traversal,
     * and when it is less than 0, it will be called at the end.
     */
    val order: Int
        get() = 0


    fun registerListener(context: Context) {
        EventBus.register(this)
        initContext(context)
    }

    fun unregisterListener() {
        EventBus.unregister(this)
    }

    fun initContext(context: Context) {}
}