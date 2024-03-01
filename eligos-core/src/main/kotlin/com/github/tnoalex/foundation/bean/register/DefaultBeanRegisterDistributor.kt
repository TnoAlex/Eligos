package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.utils.loadServices
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

object DefaultBeanRegisterDistributor : BeanRegisterDistributor {
    override fun dispatch() {
        val dispatcherMap = HashMap<String, BeanRegister>()
        val containerMap = HashMap<BeanScope, BeanContainer>()

        loadServices(BeanRegister::class.java).forEach {
            dispatcherMap[it::class.simpleName!!] = it
        }
        ApplicationContext.visitContainers { beanScope, beanContainer ->
            if (beanContainer.containerId == 0) {
                containerMap[beanScope] = beanContainer
            }
        }
        scanClasses()?.run {
            sortedByDescending { getComponentOrder(it) }.forEach {
                val annotation = it.getAnnotation(Component::class.java)
                val registerName = annotation.beanRegister
                val beanName = annotation.beanName.ifEmpty { it.simpleName }
                val scope = annotation.scope
                val register =
                    dispatcherMap[registerName]
                        ?: throw RuntimeException("Can not find bean factory named '$registerName'")
                register.registerBean(
                    beanName,
                    it,
                    containerMap[scope] ?: throw RuntimeException("Invalid scope: '$scope'")
                )
            }
        }
    }

    private fun scanClasses(): MutableSet<Class<*>>? {
        val reflection = Reflections(
            ConfigurationBuilder()
                .setClassLoaders(arrayOf(Thread.currentThread().contextClassLoader))
                .forPackages("")
                .addScanners(Scanners.TypesAnnotated)
        )
        return reflection.getTypesAnnotatedWith(Component::class.java)
    }

    private fun getComponentOrder(bean: Class<*>): Int {
        return (bean.annotations.find { it.annotationClass == Component::class }!! as Component).order
    }
}