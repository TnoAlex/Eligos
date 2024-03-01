package com.github.tnoalex.foundation.bean.container

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class DefaultBeanContainerScanner : BeanContainerScanner {
    override fun scanBeanContainers(): List<Class<out BeanContainer>>? {
        return Reflections(
            ConfigurationBuilder()
                .forPackages("")
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanContainer::class.java)?.toList()
    }
}