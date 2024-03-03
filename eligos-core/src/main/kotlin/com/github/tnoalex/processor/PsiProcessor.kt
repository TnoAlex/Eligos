package com.github.tnoalex.processor

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LanguageSupportInfo
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.eventbus.EventBus

interface PsiProcessor : LanguageSupportInfo {
    fun registerListener(context: Context) {
        EventBus.register(this)
        initContext(context)
    }

    fun unregisterListener() {
        EventBus.unregister(this)
    }

    fun initContext(context: Context) {}
}