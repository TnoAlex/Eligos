package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.common.CollectionContainer
import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

object AstProcessorContainer : CollectionContainer<String, AstProcessor> {
    private val processors = HashMap<String, LinkedList<AstProcessor>>()
    private val logger = LoggerFactory.getLogger(AstProcessorContainer::class.java)

    init {
        loadServices(AstProcessor::class.java).forEach {
            it.hookAst()
            register(it)
        }
    }

    override fun register(entity: AstProcessor) {
        entity.supportLanguage.forEach {
            if (processors[it] == null) {
                processors[it] = LinkedList(listOf(entity))
            } else {
                processors[it]!!.add(entity)
            }
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


    fun hookAstByLang(lang: String) {
        processors[lang]?.forEach {
            it.hookAst()
            logger.info("Hooked ${it::class.simpleName}")
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