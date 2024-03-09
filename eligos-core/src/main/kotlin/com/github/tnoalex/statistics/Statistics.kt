package com.github.tnoalex.statistics

interface Statistics {
    var fileNumber: Int
    var lineNumber: Int
    fun unwrap(): Map<String, String>
}