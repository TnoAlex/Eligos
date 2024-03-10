package com.github.tnoalex.statistics

import com.github.tnoalex.specs.FormatterSpec
import kotlin.reflect.full.memberProperties

data class KotlinStatistics(
    override var fileNumber: Int = 0,
    override var lineNumber: Int = 0,
    var classNumber: Int = 0,
    var functionNumber: Int = 0,
    var propertyNumber: Int =0
) : Statistics {
    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        return LinkedHashMap(this::class.memberProperties.associate { it.name to (it.getter.call(this) as Int).toString() })
    }
}
