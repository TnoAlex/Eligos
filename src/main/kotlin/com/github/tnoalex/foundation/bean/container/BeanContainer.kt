package com.github.tnoalex.foundation.bean.container

import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope

interface BeanContainer {
    val scope: BeanScope
    val containerId: Int

    fun addBean(beanName: String, bean: Any)
    fun removeBean(beanName: String): Boolean
    fun removeBean(beanType: Class<*>): Boolean
    fun getBean(beanName: String): Any?
    fun getBean(beanType: Class<*>): List<Any>?
    fun containsBean(beanName: String): Boolean {
        return BeanNameManager.containsBean(beanName)
    }

    fun containsBean(beanType: Class<*>): Boolean {
        return BeanNameManager.containsBean(beanType)
    }
}