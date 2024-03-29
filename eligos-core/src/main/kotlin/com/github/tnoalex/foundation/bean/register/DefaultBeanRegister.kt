package com.github.tnoalex.foundation.bean.register

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.container.BeanContainer

class DefaultBeanRegister : BeanRegister {
    override fun registerBean(beanName: String, beanClass: Class<*>, beanContainer: BeanContainer) {
        val beanInstance = beanClass.getDeclaredConstructor().newInstance()
        ApplicationContext.beanPreRegisterHandler.handle(beanInstance)
        beanContainer.addBean(beanName, beanInstance)
        ApplicationContext.beanPostRegisterHandler.handle(beanInstance)
    }
}