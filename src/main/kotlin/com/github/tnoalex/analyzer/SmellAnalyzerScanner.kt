package com.github.tnoalex.analyzer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SmellAnalyzerScanner(val value: String)
