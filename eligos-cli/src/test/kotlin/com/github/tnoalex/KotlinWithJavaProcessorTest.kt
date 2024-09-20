package com.github.tnoalex

import com.github.tnoalex.foundation.EligosBeforeAllTestExtension
import com.github.tnoalex.foundation.RequireTestProcessor
import com.github.tnoalex.issues.kotlin.withJava.*
import com.github.tnoalex.processor.kotlin.withJava.*
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(EligosBeforeAllTestExtension::class)
class KotlinWithJavaProcessorTest {

    @RequireTestProcessor("resources@ignoreException")
    fun testIgnoredException(processor: IgnoredExceptionProcessor) {
        psiFiles().forEach { psiFile ->
            processor.process(psiFile)
        }
        val ignoredExceptionIssue = issue<IgnoredExceptionIssue>()
        assertEquals(1, ignoredExceptionIssue.size)
        assertArrayEquals(
            arrayOf(8, "java.io.IOException", true),
            ignoredExceptionIssue.firstOrNull()?.let {
                arrayOf(it.startLine, it.ignoredExceptions, it.calledByJava)
            }
        )
    }

    @RequireTestProcessor("resources@incomprehensibleJavaFacadeName")
    fun testIncomprehensibleJavaFacadeName(processor: IncomprehensibleJavaFacadeNameProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val incomprehensibleJavaFacadeNameIssue = issue<IncomprehensibleJavaFacadeNameIssue>()
        assertEquals(1, incomprehensibleJavaFacadeNameIssue.size)
        assertArrayEquals(
            arrayOf(true, true, "IncomprehensibleClassNameKt"),
            incomprehensibleJavaFacadeNameIssue.firstOrNull()?.let {
                arrayOf(it.hasTopLevelFunction, it.hasTopLevelProperty, it.javaFacadeName)
            }
        )
    }

    @RequireTestProcessor("resources@internalExposed")
    fun testInternalExposed(processor: InternalExposedProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is PsiJavaFile) {
                processor.process(psiFile)
            }
        }
        val internalExposedIssue = issue<InternalExposedIssue>()
        assertEquals(3, internalExposedIssue.size)
        assertArrayEquals(
            arrayOf("internaltest.java.UseInternalInJava0", "internaltest.kotlin.InternalOpenClassInKotlin", true),
            internalExposedIssue.firstOrNull {
                it.affectedFiles.find { f -> f.endsWith("UseInternalInJava0.java") } != null
            }?.let {
                arrayOf(it.javaClassFqName, it.kotlinClassFqName, it.isExtend)
            }
        )
        assertArrayEquals(
            arrayOf(
                "internaltest.java.UseInternalInJava2",
                listOf("internaltest.kotlin.InternalInterfaceInKotlin"),
                false
            ),
            internalExposedIssue.firstOrNull {
                it.affectedFiles.find { f -> f.endsWith("UseInternalInJava2.java") } != null
            }?.let {
                arrayOf(it.javaClassFqName, it.kotlinInterfacesFqNames, it.isExtend)
            }
        )
    }

    @RequireTestProcessor("resources@nonJvmFieldCompanionValue")
    fun testNonJVMFieldCompanionValue(processor: NonJVMFieldCompanionValueProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val nonJVMFieldCompanionValueIssue = issue<NonJVMFieldCompanionValueIssue>()
        assertEquals(1, nonJVMFieldCompanionValueIssue.size)
        assertArrayEquals(
            arrayOf("nonJvmFieldCompanionValue.Test.Companion.strts", 6),
            nonJVMFieldCompanionValueIssue.firstOrNull()?.let {
                arrayOf(it.propertyName, it.startLine)
            }
        )
    }

    @RequireTestProcessor("resources@nonJVMStaticCompanionFunction")
    fun testNonJVMStaticCompanionFunction(processor: NonJVMStaticCompanionFunctionProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val nonJVMStaticCompanionFunctionIssue = issue<NonJVMStaticCompanionFunctionIssue>()
        assertEquals(1, nonJVMStaticCompanionFunctionIssue.size)
        assertArrayEquals(
            arrayOf("nonJVMStaticCompanionFunction.Test.Companion.test()", 5),
            nonJVMStaticCompanionFunctionIssue.firstOrNull()?.let {
                arrayOf(it.functionSignature, it.startLine)
            }
        )
    }

    @RequireTestProcessor("resources@provideImmutableCollection")
    fun testProvideImmutableCollection(processor: ProvideImmutableCollectionProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is PsiJavaFile) {
                processor.process(psiFile)
            }
        }
        val provideImmutableCollectionIssue = issue<ProvideImmutableCollectionIssue>()
        assertEquals(1, provideImmutableCollectionIssue.size)
        assertArrayEquals(
            arrayOf("provideImmutableCollection.kotlin.pInKotlin", "provideImmutableCollection.java.UseInJava", true),
            provideImmutableCollectionIssue.firstOrNull()?.let {
                arrayOf(it.providerKtElementFqName, it.useJavaClassFqName, it.isFunction)
            }
        )
    }

    @RequireTestProcessor("resources@unclearPlatformType")
    fun testUncertainNullablePlatformType(processor: UncertainNullablePlatformTypeProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val propertyPlatformType = issue<UncertainNullablePlatformTypeInPropertyIssue>()
        val expressionPlatformType = issue<UncertainNullablePlatformExpressionUsageIssue>()
        val callerPlatformType = issue<UncertainNullablePlatformCallerIssue>()
        assertEquals(5, propertyPlatformType.size)
        assertEquals(2, expressionPlatformType.size)
        assertEquals(2, callerPlatformType.size)
    }
}