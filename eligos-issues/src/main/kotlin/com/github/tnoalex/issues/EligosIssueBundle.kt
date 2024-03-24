package com.github.tnoalex.issues

import com.github.tnoalex.foundation.bundle.AbstractBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val ELIGOS_ISSUES = "strings.issue.eligos-issue"

object EligosIssueBundle : AbstractBundle(ELIGOS_ISSUES) {
    fun message(
        @PropertyKey(resourceBundle = ELIGOS_ISSUES) key: String,
        vararg params: Any,
    ): String = getMessage(key, *params)

    fun lazy(
        @PropertyKey(resourceBundle = ELIGOS_ISSUES) key: String,
        vararg params: Any,
    ): () -> String = getLazyMessage(key, *params)
}