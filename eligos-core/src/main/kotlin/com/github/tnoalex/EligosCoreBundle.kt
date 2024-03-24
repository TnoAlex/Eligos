package com.github.tnoalex

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val ELIGOS_BUNDLE = "eligos-meta"
object EligosCoreBundle:AbstractBundle(ELIGOS_BUNDLE) {
    fun message(
        @PropertyKey(resourceBundle = ELIGOS_BUNDLE) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = ELIGOS_BUNDLE) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}