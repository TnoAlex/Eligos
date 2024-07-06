package com.github.tnoalex.foundation

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import org.jetbrains.kotlin.references.fe10.base.DummyKtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext


class EligosForEachTestExtension : BeforeEachCallback, AfterEachCallback {

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
        ApplicationContext.removeBeanOfType(DummyKtFe10ReferenceResolutionHelper::class.java)
        ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!.resetEnvironment()
        ApplicationContext.removeBeanOfType(CliCompilerEnvironmentContext::class.java)
    }
}