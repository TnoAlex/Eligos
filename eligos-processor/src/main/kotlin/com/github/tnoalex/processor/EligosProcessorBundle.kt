package com.github.tnoalex.processor

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey

private const val ELIGOS_PROCESSOR = "strings.processor.eligos-processor"

object EligosProcessorBundle : AbstractBundle(ELIGOS_PROCESSOR) {
    fun message(
        @PropertyKey(resourceBundle = ELIGOS_PROCESSOR) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = ELIGOS_PROCESSOR) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}