package com.github.tnoalex.foundation.asttools.kotlin

import com.github.tnoalex.foundation.asttools.AstParser
import depends.extractor.kotlin.KotlinLexer
import depends.extractor.kotlin.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

object KotlinAstParser : AstParser {
    override val supportLanguage: String
        get() = "kotlin"

    override fun parseAst(fileName: String) {
        val input = CharStreams.fromFileName(fileName)
        val lexer = KotlinLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = KotlinParser(tokens)
        val bridge = KotlinAstListener()
        val walker = ParseTreeWalker()
        walker.walk(bridge, parser.kotlinFile())
    }
}