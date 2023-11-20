package com.github.tnoalex.analyzer.singlelang.kotlin

import com.github.tnoalex.analyzer.singlelang.SingleLangAbstractSmellAnalyzer


class KotlinSmellAnalyzer : SingleLangAbstractSmellAnalyzer() {
    override val supportLanguage: String
        get() = "kotlin"

    override fun analyze() {
        TODO("Not yet implemented")
    }

    fun findLongMethod() {

    }
}