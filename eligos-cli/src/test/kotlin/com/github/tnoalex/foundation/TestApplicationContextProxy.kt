package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.BeanScope
import com.github.tnoalex.foundation.bean.container.BeanContainer
import com.github.tnoalex.foundation.bean.handler.BeanHandler
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.utils.invokeProperty0Getter
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.full.declaredMemberProperties

object TestApplicationContextProxy {

    private fun getProxyProperty(propertyName: String): Any? {
        return ApplicationContext::class.declaredMemberProperties
            .find { it.name == propertyName }?.let { prop ->
                val beanPreRemoveHandler = invokeProperty0Getter(prop)
                beanPreRemoveHandler
            }
    }

    @TestOnly
    @Suppress("unused")
    fun getBeanPreRemoveHandler(): BeanHandler {
        return getProxyProperty("beanPreRemoveHandler") as? BeanHandler ?: throw RuntimeException()
    }

    @TestOnly
    @Suppress("unused")
    fun getBeanAfterRemoveHandler(): BeanHandler {
        return getProxyProperty("beanAfterRemoveHandler") as? BeanHandler ?: throw RuntimeException()
    }

    @TestOnly
    @Suppress("unused")
    fun getBeanPreRegisterHandler(): BeanHandler {
        return getProxyProperty("beanPreRegisterHandler") as? BeanHandler ?: throw RuntimeException()
    }

    @TestOnly
    @Suppress("unused")
    fun getBeanPostRegisterHandler(): BeanHandler {
        return getProxyProperty("beanPostRegisterHandler") as? BeanHandler ?: throw RuntimeException()
    }

    @TestOnly
    @Suppress("unused")
    fun getBeansAfterRegisterHandler(): BeanHandler {
        return getProxyProperty("beansAfterRegisterHandler") as? BeanHandler ?: throw RuntimeException()
    }

    @TestOnly
    @Suppress("unused", "UNCHECKED_CAST")
    fun addBeanContainer(scope: BeanScope, container: BeanContainer) {
        val beanContainers = getProxyProperty("beanContainers") as? HashMap<BeanScope, ArrayList<BeanContainer>>
            ?: throw RuntimeException()
        beanContainers.getOrPut(scope) { arrayListOf() }.add(container)
    }
}