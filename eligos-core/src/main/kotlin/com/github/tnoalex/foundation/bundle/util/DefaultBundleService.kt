package com.github.tnoalex.foundation.bundle.util

import java.util.function.Supplier

class DefaultBundleService {
    fun <T> compute(computable: Supplier<out T?>): T? {
        val isDefault = isDefaultBundle
        if (!isDefault) {
            ourDefaultBundle.set(true)
        }
        try {
            return computable.get()
        } finally {
            if (!isDefault) {
                ourDefaultBundle.set(false)
            }
        }
    }

    companion object {
        val instance: DefaultBundleService = DefaultBundleService()
        private val ourDefaultBundle: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

        val isDefaultBundle: Boolean
            get() = ourDefaultBundle.get()
    }
}
