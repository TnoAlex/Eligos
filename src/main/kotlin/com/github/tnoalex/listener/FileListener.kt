package com.github.tnoalex.listener

import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.processor.AbstractBaseInfoProcessor
import depends.extractor.IFileListener

object FileListener : IFileListener {
    override fun enterFile(fileName: String): Boolean {
        EventBus.post("enter@$fileName", listOf(AbstractBaseInfoProcessor::class))
        return true
    }

    override fun exitFile(fileName: String) {
        EventBus.post("exit@$fileName", listOf(AbstractBaseInfoProcessor::class))
    }
}