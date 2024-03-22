package com.github.tnoalex.issues

import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.utils.getDeclaredMemberProperties
import com.github.tnoalex.utils.invokePropertyGetter
import com.github.tnoalex.utils.isAnnotatedWith

fun Issue.unpackingIssue(): LinkedHashMap<String, Any> {
    val propertyMap = LinkedHashMap<String, Any>()
    getDeclaredMemberProperties(this::class).forEach {
        if (it.isAnnotatedWith(UnpackIgnore::class)) return@forEach
        propertyMap[it.name] = invokePropertyGetter(this, it) ?: return@forEach
    }
    return propertyMap
}