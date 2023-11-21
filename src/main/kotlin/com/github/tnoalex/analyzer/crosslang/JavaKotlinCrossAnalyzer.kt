package com.github.tnoalex.analyzer.crosslang

class JavaKotlinCrossAnalyzer : AbstractCrossLangAnalyzer() {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    override fun analyze() {
        super.analyze()
    }
}