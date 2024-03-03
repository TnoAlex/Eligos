package com.github.tnoalex.foundation.bean.container

import com.github.tnoalex.foundation.addAllJars
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class DefaultBeanContainerScanner : BeanContainerScanner {
    override fun scanBeanContainers(): List<Class<out BeanContainer>>? {
        return Reflections(
            ConfigurationBuilder()
                .addAllJars()
                .forPackages("")
                .setScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanContainer::class.java)?.toList()
    }
}