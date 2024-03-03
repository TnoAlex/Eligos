package com.github.tnoalex.foundation.bean.handler

import com.github.tnoalex.foundation.addAllJars
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class DefaultBeanHandlerScanner : BeanHandlerScanner {
    override fun scanBeanHandler(): List<Class<out BeanHandler>>? {
        return Reflections(
            ConfigurationBuilder()
                .addAllJars()
                .forPackages("")
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanHandler::class.java)?.toList()
    }
}