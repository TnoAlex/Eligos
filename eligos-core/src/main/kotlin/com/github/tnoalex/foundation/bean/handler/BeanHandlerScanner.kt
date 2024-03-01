package com.github.tnoalex.foundation.bean.handler

fun interface BeanHandlerScanner {
    fun scanBeanHandler(): List<Class<out BeanHandler>>?
}