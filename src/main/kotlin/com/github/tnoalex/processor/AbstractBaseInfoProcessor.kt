package com.github.tnoalex.processor

import com.github.tnoalex.foundation.eventbus.EventListener

abstract class AbstractBaseInfoProcessor : PsiProcessorWithContext() {
    @EventListener
    protected var currentFileName: String = ""
        set(value) {
            field = value.split("@")[1]
        }


}