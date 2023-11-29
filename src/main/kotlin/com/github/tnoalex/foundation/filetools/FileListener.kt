package com.github.tnoalex.foundation.filetools

import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.processor.kotlin.BaseInfoProcessor
import depends.extractor.IFileListener

object FileListener : IFileListener {
    override fun enterFile(fileName: String): Boolean {
        EventBus.post("enter@$fileName", listOf(BaseInfoProcessor::class))
        return true
    }

    override fun exitFile(fileName: String) {
        EventBus.post("exit@$fileName", listOf(BaseInfoProcessor::class))
    }
}