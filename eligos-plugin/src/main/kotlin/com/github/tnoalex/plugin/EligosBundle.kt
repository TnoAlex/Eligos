package com.github.tnoalex.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val ELIGOS_BUNDLE = "strings.eligosBundle"

object EligosBundle : DynamicBundle(ELIGOS_BUNDLE) {
    @Nls
    fun message(
        @PropertyKey(resourceBundle = ELIGOS_BUNDLE) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    @Nls
    fun lazy(
        @PropertyKey(resourceBundle = ELIGOS_BUNDLE) key: String,
        vararg params: Any,
    ): Supplier<String> = getLazyMessage(key, *params)
}