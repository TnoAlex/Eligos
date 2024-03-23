package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.utils.scanEntries
import org.reflections.scanners.Scanners
import java.util.*

@Component(order = Int.MAX_VALUE - 2)
class PropertyManager {
    private val eligosProperties = HashMap<String, HashMap<String, String>>()

    init {
        val classLoader = ApplicationContext.currentClassLoader
        val resources = scanEntries(Scanners.Resources).getResources(PROPERTIES_PATTERN)
        resources.forEach {
            val inputStream = classLoader.getResourceAsStream(it) ?: return@forEach
            val props = Properties()
            props.load(inputStream)
            props.forEach { (k, v) ->
                require(k is String)
                require(v is String)
                val (majorKey, minorKey) = k.split(".")
                eligosProperties.getOrPut(majorKey) { hashMapOf(minorKey to v) }[minorKey] = v
            }
        }
    }

    fun getMajorMap(majorKey: String): HashMap<String, String>? {
        return eligosProperties[majorKey]
    }

    fun getPropertyValue(majorKey: String, minorKey: String): Any? {
        return eligosProperties[majorKey]?.get(minorKey)
    }

    companion object {
        private const val PROPERTIES_PATTERN = "^eligos.*\\.properties$"
    }
}