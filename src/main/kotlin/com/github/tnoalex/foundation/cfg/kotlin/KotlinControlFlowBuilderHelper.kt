package com.github.tnoalex.foundation.cfg.kotlin

import com.github.tnoalex.foundation.cfg.ControlFlowBuilderHelper
import depends.extractor.kotlin.KotlinLexer
import depends.extractor.kotlin.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

object KotlinControlFlowBuilderHelper : ControlFlowBuilderHelper {
    override fun startBuild(fileName: String) {
        val byteStream = CharStreams.fromFileName(fileName)
        val lexer = KotlinLexer(byteStream)
        val token = CommonTokenStream(lexer)
        val parser = KotlinParser(token)
        val bridge = KotlinListener(KotlinControlFlowBuilder(fileName))
        val walker = ParseTreeWalker()
        walker.walk(bridge, parser.kotlinFile())
    }
}