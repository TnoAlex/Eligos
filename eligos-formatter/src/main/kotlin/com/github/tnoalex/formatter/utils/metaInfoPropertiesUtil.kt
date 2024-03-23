package com.github.tnoalex.formatter.utils

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.PropertyManager
import java.util.HashMap

internal fun getMetaInfoProperties(): HashMap<String, String>? {
    return ApplicationContext.getExactBean(PropertyManager::class.java)!!.getMajorMap("meta")
}