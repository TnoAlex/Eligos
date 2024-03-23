package com.github.tnoalex.issues

import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.PropertyManager
import com.github.tnoalex.utils.getMemberProperties
import com.github.tnoalex.utils.invokePropertyGetter
import com.github.tnoalex.utils.isAnnotatedWith

fun Issue.unpackingIssue(): LinkedHashMap<String, Any> {
    val propertyMap = LinkedHashMap<String, Any>()
    getMemberProperties(this::class).forEach {
        if (it.isAnnotatedWith(UnpackIgnore::class)) return@forEach
        propertyMap[it.name] = invokePropertyGetter(this, it) ?: return@forEach
    }
    return propertyMap
}

fun getIssueNameFormProperties(clazzName: String): String {
    val propertiesMap = ApplicationContext.getExactBean(PropertyManager::class.java)!!.getMajorMap("issue")
        ?: return "UnKnown Issue Name"
    return propertiesMap[clazzName] ?: "UnKnown Issue Name"
}