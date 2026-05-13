package com.github.tnoalex.foundation

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method


class EligosForEachTestExtension : BeforeEachCallback, AfterEachCallback, InvocationInterceptor {

    override fun beforeEach(context: ExtensionContext) {
        ApplicationContext.addBean(Context::class.simpleName!!, Context(), SimpleSingletonBeanContainer)
        ApplicationContext.invokeAfterBeansRegisterHandler()
    }

    override fun afterEach(context: ExtensionContext) {
        require(context.requiredTestMethod.parameters.size == 1)
        val requireTestParams = context.requiredTestMethod.parameters[0]
        context.requiredTestMethod.getAnnotation(RequireTestProcessor::class.java).injectedBeans.forEach {
            ApplicationContext.removeBeanOfType(it.java)
        }
        val testProcessor = requireTestParams.type.kotlin
        ApplicationContext.removeBeanOfType(testProcessor.java)
        ApplicationContext.removeBeanOfType(Context::class.java)
        ApplicationContext.removeBeanOfType(DataFlowValueFactory::class.java)
        ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!.resetEnvironment()
        ApplicationContext.removeBeanOfType(CliCompilerEnvironmentContext::class.java)
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void?>,
        invocationContext: ReflectiveInvocationContext<Method?>?,
        extensionContext: ExtensionContext?
    ) {
        val env = ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!
        analyze(env.module) {
            val ctx = ApplicationContext.getExactBean(Context::class.java)!!
            ctx.session = this
            invocation.proceed()
            ctx.session = null
        }
    }

    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void?>,
        invocationContext: ReflectiveInvocationContext<Method?>?,
        extensionContext: ExtensionContext?
    ) {
        val env = ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!
        analyze(env.module) {
            val ctx = ApplicationContext.getExactBean(Context::class.java)!!
            ctx.session = this
            invocation.proceed()
            ctx.session = null
        }
    }
}