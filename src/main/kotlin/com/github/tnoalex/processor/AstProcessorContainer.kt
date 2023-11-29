package com.github.tnoalex.processor

import com.github.tnoalex.Context
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

    override fun getByKey(key: String): List<AstProcessor>? = processors[key]?.sortedByDescending { it.order }

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


    fun registerByLang(lang: String,context: Context) {
        val langProcessors = listOf(processors[lang] ?: LinkedList(), processors["any"] ?: LinkedList()).flatten()
        langProcessors.run {
            sortedByDescending { it.order }
            forEach {
                it.registerListener(context)
                logger.info("Hooked ${it::class.simpleName}")
            }
        }
    }

    fun registerAllProcessor(context: Context) {
        processors.values.forEach {
            it.sortedByDescending { p -> p.order }
                .forEach { p ->
                    p.registerListener(context)
                }
        }
    }

    fun unregistersByLang(lang: String) {
        processors[lang]?.forEach {
            it.unregisterListener()
        }
    }

    fun cleanUpProcessor(lang: String) {
        processors.remove(lang)?.forEach {
            it.unregisterListener()
            logger.info("Removed AstProcessor: ${it::class.simpleName}")
        }
    }

    fun unregisterAllProcessor() {
        processors.values.forEach {
            it.forEach { p ->
                p.unregisterListener()
            }
        }
    }
}