package com.github.tnoalex.analyzer.singlelang.kotlin

import com.github.tnoalex.analyzer.singlelang.AbstractSingleLangAnalyzer


class KotlinSmellAnalyzer : AbstractSingleLangAnalyzer() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    override fun analyze() {
        super.analyze()
    }

    fun findLongMethod() {

    }
}