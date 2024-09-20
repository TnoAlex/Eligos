package com.github.tnoalex.foundation.bean.container

import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope

interface BeanContainer {
    val scope: BeanScope
    val containerId: Int

    fun addBean(beanName: String, bean: Any)
    fun removeBean(beanName: String): Any?
    fun removeBeanOfType(beanType: Class<*>): List<Any>
    fun getBean(beanName: String): Any?

    fun visitBeans(visitor: (String, Any) -> Unit)
    fun <T> getBeanOfType(beanType: Class<T>): List<T>?

    fun <T> getExactBean(beanType: Class<T>): T?
    fun containsBean(beanName: String): Boolean {
        return BeanNameManager.containsBean(beanName)
    }

    fun containsBean(beanType: Class<*>): Boolean {
        return BeanNameManager.containsBean(beanType)
    }
}