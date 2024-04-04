package com.github.tnoalex.processor

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LanguageSupportInfo
import com.github.tnoalex.foundation.eventbus.EventBus

interface BaseProcessor : LanguageSupportInfo {
    val context: Context
        get() = ApplicationContext.getExactBean(Context::class.java)!!

    fun registerListener() {
        EventBus.register(this)
    }

    fun unregisterListener() {
        EventBus.unregister(this)
    }
}