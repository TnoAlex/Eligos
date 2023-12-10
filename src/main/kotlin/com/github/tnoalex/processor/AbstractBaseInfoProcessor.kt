package com.github.tnoalex.processor

import com.github.tnoalex.foundation.eventbus.EventListener

abstract class AbstractBaseInfoProcessor : AstProcessorWithContext() {
    override val order: Int
        get() = Int.MAX_VALUE

    @EventListener
    protected var currentFileName: String = ""
        set(value) {
            field = value.split("@")[1]
        }


}