package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.utils.scanEntries
import org.reflections.scanners.Scanners

class DefaultBeanRegisterDistributor : BeanRegisterDistributor {
    override fun scanDispatchers(): List<Class<out BeanRegister>>? {
        return scanEntries(Scanners.SubTypes).getSubTypesOf(BeanRegister::class.java)?.toList()
    }

    override fun scanComponent(): List<Class<*>>? {
        val reflection = scanEntries(Scanners.TypesAnnotated)
        return reflection.getTypesAnnotatedWith(Component::class.java)?.sortedByDescending { getComponentOrder(it) }
    }

    private fun getComponentOrder(bean: Class<*>): Int {
        return (bean.annotations.find { it.annotationClass == Component::class }!! as Component).order
    }
}