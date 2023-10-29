package com.github.tnoalex

import com.github.tnoalex.analyzer.SmellAnalyzerRegister
import com.github.tnoalex.analyzer.SmellAnalyzerScanner
import com.github.tnoalex.cli.CommandParser


fun main(args: Array<String>) {
    SmellAnalyzerRegister.INSTANCE.init()
    CommandParser().main(args)
}