package com.github.tnoalex.foundation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@ParameterizedTest
@ExtendWith(EligosForEachTestExtension::class)
@ArgumentsSource(EligosTestArgumentsProvider::class)
annotation class RequireTestProcessor(val testResourcePath: String)
