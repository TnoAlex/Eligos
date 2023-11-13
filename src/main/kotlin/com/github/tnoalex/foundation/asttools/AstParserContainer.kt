package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.utils.loadServices
import java.io.File

object AstParserContainer {
    private val parsers = HashMap<String, AstParser>()

    init {
        loadServices(AstParser::class.java).forEach {
            parsers[it.supportLanguage] = it
        }
    }

    fun parseFilesByLang(lang: String) {
        val parser = parsers[lang] ?: return
        AstProcessorContainer.hookAstByLang(lang)
        FileContainer.visitSourcesFile {
            parser.parseAst(it.absolutePath)
        }
    }

    fun parseFilesByLang(lang: String, filter: (File) -> Boolean) {
        val parser = parsers[lang] ?: return
        AstProcessorContainer.hookAstByLang(lang)
        FileContainer.visitSourcesFile {
            if (filter(it)) {
                parser.parseAst(it.absolutePath)
            }
        }
    }

    fun parseFiles() {
        AstProcessorContainer.hookAllProcessor()
        FileContainer.visitSourcesFile {
            parsers.values.forEach { p ->
                p.parseAst(it.absolutePath)
            }
        }
    }

    fun parseFile(filter: (File) -> Boolean) {
        AstProcessorContainer.hookAllProcessor()
        FileContainer.visitSourcesFile {
            parsers.values.forEach { p ->
                if (filter(it)) {
                    p.parseAst(it.absolutePath)
                }
            }
        }
    }
}