package com.github.tnoalex.foundation.bean.container

fun interface BeanContainerScanner {
    fun scanBeanContainers(): List<Class<out BeanContainer>>?
}