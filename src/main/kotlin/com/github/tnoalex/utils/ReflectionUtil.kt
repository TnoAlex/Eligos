package com.github.tnoalex.utils

import kotlin.reflect.KClass

fun <T : Any> getAllSubClass(kClass: KClass<T>): ArrayList<KClass<*>> {
    val subClasses = kClass.sealedSubclasses
    val result = ArrayList<KClass<*>>()
    subClasses.forEach {
        val list = ArrayList<KClass<*>>()
        getSubClass(it,list)
        result.addAll(list)
    }
    return result
}

private fun <T : Any> getSubClass(kClass: KClass<T>, list: ArrayList<KClass<*>>) {
    if(kClass.sealedSubclasses.isEmpty()){
        return
    }
    kClass.sealedSubclasses.forEach {
        list.add(it)
        getSubClass(it,list)
    }
}