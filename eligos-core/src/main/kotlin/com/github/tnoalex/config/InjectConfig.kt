package com.github.tnoalex.config

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectConfig(val configKey: String)
