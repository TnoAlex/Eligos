package com.github.tnoalex.foundation.asttools.processor.kotlin

import com.github.tnoalex.foundation.asttools.processor.AstProcessor
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.foundation.eventbus.EventListener
import depends.extractor.kotlin.KotlinParser.KotlinFileContext

class BaseInfoProcessor : AstProcessor {
    override val order: Int
        get() = Int.MAX_VALUE

    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @set:EventListener
    private var currentFileName: String = ""
        set(value) {
            field = value
        }

    override fun registerListener() {
        EventBus.register(this)
    }

    override fun unregisterListener() {
        EventBus.unregister(this)
    }

    @EventListener
    fun enterFile(ctx: KotlinFileContext) {

//        val fileElement = FileElement(
//            ctx.
        //          text
//        )
    }

    fun exitFile(ctx: KotlinFileContext) {

    }
}