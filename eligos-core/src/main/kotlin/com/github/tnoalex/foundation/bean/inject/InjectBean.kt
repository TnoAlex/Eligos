package com.github.tnoalex.foundation.bean.inject

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectBean(val beanName: String = "", val beanType: KClass<*> = Any::class)