package com.github.tnoalex.foundation.bean

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Component(
    val beanName: String = "",
    val beanRegister: String = "DefaultBeanRegister",
    val scope: BeanScope = BeanScope.Singleton,
    val order: Int = -1
)
