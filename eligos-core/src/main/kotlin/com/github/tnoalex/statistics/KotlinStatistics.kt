package com.github.tnoalex.statistics

import kotlin.reflect.full.memberProperties

data class KotlinStatistics(
    override var fileNumber: Int = 0,
    override var lineNumber: Int = 0,
    var classNumber: Int = 0,
    var functionNumber: Int = 0,
    var propertyNumber: Int =0
) : Statistics {
    override fun unwrap(): Map<String, String> {
        return this::class.memberProperties.associate { it.name to (it.getter.call(this) as Int).toString() }
    }
}
