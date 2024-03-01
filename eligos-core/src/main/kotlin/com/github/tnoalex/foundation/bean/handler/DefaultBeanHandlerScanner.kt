package com.github.tnoalex.foundation.bean.handler

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class DefaultBeanHandlerScanner : BeanHandlerScanner {
    override fun scanBeanHandler(): List<Class<out BeanHandler>>? {
        return Reflections(
            ConfigurationBuilder()
                .forPackages("")
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanHandler::class.java)?.toList()
    }
}