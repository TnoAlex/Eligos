package com.github.tnoalex.processor

import com.github.tnoalex.foundation.common.LanguageSupportInfo
import com.github.tnoalex.foundation.eventbus.EventBus

interface AstProcessor : LanguageSupportInfo {
    val order: Int
        get() = 0

    fun registerListener(){
        EventBus.register(this)
    }
    fun unregisterListener(){
        EventBus.unregister(this)
    }
}