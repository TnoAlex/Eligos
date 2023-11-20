package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.common.CollectionContainer
import com.github.tnoalex.utils.loadServices
import java.util.*
import kotlin.reflect.KClass

object AstProcessorContainer : CollectionContainer<AstProcessor> {
    private val processors = HashMap<String, LinkedList<AstProcessor>>()

    init {
        loadServices(AstProcessor::class.java).forEach {
            it.hookAst()
            register(it)
        }
    }

    override fun register(entity: AstProcessor) {
        if (processors[entity.supportLanguage] == null) {
            processors[entity.supportLanguage] = LinkedList(listOf(entity))
        } else {
            processors[entity.supportLanguage]!!.add(entity)
        }
    }

    override fun getByKey(key: String): LinkedList<AstProcessor>? = processors[key]

    override fun getKeys(): List<String> = processors.keys.toList()

    override fun getByType(clazz: KClass<out AstProcessor>): List<AstProcessor>? {
        processors.values.forEach {
            val res = it.filter { p ->
                p::class == clazz
            }
            if (res.isNotEmpty()) {
                return res
            }
        }
        return null
    }

    fun getProcessByLang(lang: String): LinkedList<AstProcessor>? {
        return processors[lang]
    }

    fun hookAstByLang(lang: String) {
        processors[lang]?.forEach {
            it.hookAst()
        }
    }

    fun hookAllProcessor() {
        processors.values.forEach {
            it.forEach { p ->
                p.hookAst()
            }
        }
    }

    fun removeHooksByLang(lang: String) {
        processors[lang]?.forEach {
            it.removeHooks()
        }
    }

    fun removeAllHooks() {
        processors.values.forEach {
            it.forEach { p ->
                p.removeHooks()
            }
        }
    }
}