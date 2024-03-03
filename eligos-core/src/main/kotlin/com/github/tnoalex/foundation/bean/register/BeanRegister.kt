package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.bean.container.BeanContainer

interface BeanRegister {
    fun registerBean(beanName: String, beanClass: Class<*>, beanContainer: BeanContainer)
}