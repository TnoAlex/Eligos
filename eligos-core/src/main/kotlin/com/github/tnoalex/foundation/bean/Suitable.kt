package com.github.tnoalex.foundation.bean

import com.github.tnoalex.foundation.LaunchEnvironment

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Suitable(val environment: LaunchEnvironment)
