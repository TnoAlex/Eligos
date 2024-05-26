package com.github.tnoalex.specs

import com.github.tnoalex.utils.creatDataClassAndFillProperty
import kotlin.reflect.KClass

class SpecificationBuilder(private val rawArgs: HashMap<String, Any?>) {
    private var currentArtifact: Any? = null

    fun <T> setProperty(key: String, value: T): SpecificationBuilder {
        checkPropertyKey(key)
        rawArgs[key] = value
        return this
    }

    fun withCurrentArtifact(key: String): SpecificationBuilder {
        checkPropertyKey(key)
        rawArgs[key] = currentArtifact
        return this
    }

    fun withPartOfCurrentArtifact(key: String, mapper: (Any) -> Any?): SpecificationBuilder {
        return setProperty(key, mapper(currentArtifact!!))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> build(): T? {
        return currentArtifact as? T
    }

    fun next(targetClazz: KClass<*>): SpecificationBuilder {
        currentArtifact = creatDataClassAndFillProperty(rawArgs, targetClazz)
        return this
    }

    fun <T : Any> next(targetClazz: KClass<T>, builder: (KClass<T>, Map<String, Any?>) -> T): SpecificationBuilder {
        currentArtifact = builder(targetClazz, rawArgs)
        return this
    }

    private fun checkPropertyKey(key: String) {
        if (key in rawArgs) {
            throw IllegalArgumentException("The specification key '$key' is already in use.")
        }
    }
}