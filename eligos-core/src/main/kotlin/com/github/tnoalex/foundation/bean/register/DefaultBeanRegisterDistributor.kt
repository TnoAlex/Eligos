package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.addAllJars
import com.github.tnoalex.foundation.bean.Component
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

class DefaultBeanRegisterDistributor : BeanRegisterDistributor {
    override fun scanDispatchers(): List<Class<out BeanRegister>>? {
        return Reflections(
            ConfigurationBuilder()
                .addAllJars()
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .forPackages("")
                .addScanners(Scanners.SubTypes)
        ).getSubTypesOf(BeanRegister::class.java)?.toList()
    }

    override fun scanComponent(): List<Class<*>>? {
        val reflection = Reflections(
            ConfigurationBuilder()
                .addAllJars()
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .forPackages("")
                .addScanners(Scanners.TypesAnnotated)
        )
        return reflection.getTypesAnnotatedWith(Component::class.java)?.sortedByDescending { getComponentOrder(it) }
    }

    private fun getComponentOrder(bean: Class<*>): Int {
        return (bean.annotations.find { it.annotationClass == Component::class }!! as Component).order
    }
}