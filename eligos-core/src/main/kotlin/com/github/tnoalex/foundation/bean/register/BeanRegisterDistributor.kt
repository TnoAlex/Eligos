package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.container.BeanContainer

interface BeanRegisterDistributor {
    fun dispatch() {
        val dispatcherMap = HashMap<String, BeanRegister>()
        val containerMap = HashMap<BeanScope, BeanContainer>()

        scanRegisters()?.forEach {
            dispatcherMap[it.simpleName] = it.getDeclaredConstructor().newInstance()
        }
        ApplicationContext.visitContainers { beanScope, beanContainer ->
            if (beanContainer.containerId == 0) {
                containerMap[beanScope] = beanContainer
            }
        }
        scanComponent()?.forEach {
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

    fun scanRegisters(): List<Class<out BeanRegister>>?

    fun scanComponent(): List<Class<*>>?
}