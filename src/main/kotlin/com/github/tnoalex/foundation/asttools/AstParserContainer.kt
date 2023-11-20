package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.common.Container
import com.github.tnoalex.utils.loadServices
import kotlin.reflect.KClass

object AstParserContainer : Container<AstParser> {
    private val parsers = HashMap<String, AstParser>()

    init {
        loadServices(AstParser::class.java).forEach {
            parsers[it.supportLanguage] = it
        }
    }

    fun parseFilesByLang(lang: String, filePath: String) {
        val parser = parsers[lang] ?: return
        AstProcessorContainer.hookAstByLang(lang)
        parser.parseAst(filePath)
    }

    fun parseFiles(filePath: String) {
        AstProcessorContainer.hookAllProcessor()
        parsers.values.forEach { p ->
            p.parseAst(filePath)
        }
    }

    override fun register(entity: AstParser) {
        parsers[entity.supportLanguage] = entity
    }

    override fun getByKey(key: String): AstParser? = parsers[key]

    override fun getKeys(): List<String> = parsers.keys.toList()

    override fun getByType(clazz: KClass<out AstParser>): AstParser {
        return parsers.entries.first { it.value.javaClass == clazz.java }.value
    }
}