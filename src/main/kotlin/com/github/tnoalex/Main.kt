package com.github.tnoalex

import com.github.tnoalex.analyzer.SmellAnalyzerRegister
import com.github.tnoalex.analyzer.SmellAnalyzerScanner
import com.github.tnoalex.cli.CommandParser
import com.github.tnoalex.utils.StdOutErrWrapper


fun main(args: Array<String>) {
    StdOutErrWrapper.init()
    SmellAnalyzerRegister.INSTANCE.init()
    CommandParser().main(args)
}