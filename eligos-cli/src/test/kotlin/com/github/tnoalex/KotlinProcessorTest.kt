package com.github.tnoalex

import com.github.tnoalex.foundation.EligosBeforeAllTestExtension
import com.github.tnoalex.foundation.RequireTestProcessor
import com.github.tnoalex.issues.kotlin.*
import com.github.tnoalex.processor.kotlin.*
import org.jetbrains.kotlin.psi.KtFile
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(EligosBeforeAllTestExtension::class)
class KotlinProcessorTest {

    @RequireTestProcessor("resources@implicitSingleExprFunction")
    fun testImplicitSingleExprFunction(processor: ImplicitSingleExprFunctionProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val implicitSingleExprFunctionIssues = issue<ImplicitSingleExprFunctionIssue>()
        assertEquals(1, implicitSingleExprFunctionIssues.size)
        assertArrayEquals(
            arrayOf(6, "fun test0() = java.lang.String.valueOf(1)"),
            implicitSingleExprFunctionIssues.firstOrNull()?.let {
                arrayOf(it.startLine, it.content)
            }
        )
    }

    @RequireTestProcessor("resources@complexMethods")
    fun testKotlinMccabeComplexity(processor: KotlinMccabeComplexityProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val complexityIssues = issue<ComplexKotlinFunctionIssue>()
        assertEquals(1, complexityIssues.size)
        assertArrayEquals(
            arrayOf(10, 17),
            complexityIssues.firstOrNull()?.let {
                arrayOf(it.circleComplexity, it.startLine)
            }
        )
    }

    @RequireTestProcessor("resources@objectExtendsThrowable")
    fun testObjectExtendsThrowable(processor: ObjectExtendsThrowableProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val throwableIssues = issue<ObjectExtendsThrowableIssue>()
        assertEquals(1, throwableIssues.size)
        assertEquals(
            "objectExtendsThrowable.ExtendsThrowable",
            throwableIssues.firstOrNull()?.objectFqName
        )
    }

    @RequireTestProcessor("resources@optimizedTailRecursion")
    fun testOptimizedTailRecursion(processor: TailRecursionProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val optimizedRecursionIssues = issue<OptimizedTailRecursionIssue>()
        assertEquals(2, optimizedRecursionIssues.size)
        assertEquals(
            "optimizedTailRecursion.factorial0(n,acc)",
            optimizedRecursionIssues.firstOrNull { it.startLine == 3 }
                ?.functionSignature
        )
        assertEquals(
            "optimizedTailRecursion.factorial4(n,acc)",
            optimizedRecursionIssues.firstOrNull { it.startLine == 32 }
                ?.functionSignature
        )
    }

    @RequireTestProcessor("resources@whenInsteadOfCascadeIf")
    fun testWhenInsteadOfCascadeIf(processor: WhenInsteadOfCascadeIfProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val whenInsteadOfCascadeIf = issue<WhenInsteadOfCascadeIfIssue>()
        assertEquals(1, whenInsteadOfCascadeIf.size)
        assertArrayEquals(
            arrayOf(4, 4),
            whenInsteadOfCascadeIf.firstOrNull()?.let {
                arrayOf(it.startLine, it.cascadeDepth)
            }
        )
    }

    @RequireTestProcessor("resources@compareDataObjectWithReference")
    fun testCompareDataObjectWithReference(processor: CompareDataObjectWithReferenceProcessor) {
        psiFiles().forEach { psiFile ->
            if (psiFile is KtFile) {
                processor.process(psiFile)
            }
        }
        val compareDataObjectWithReferenceIssue = issue<CompareDataObjectWithReferenceIssue>()
        assertEquals(1, compareDataObjectWithReferenceIssue.size)
        assertArrayEquals(arrayOf(
            "compareDataObjectWithReference.useDataObjectWithReference.rdobject",
            "compareDataObjectWithReference.useDataObjectWithReference.dobject",
            8),
            compareDataObjectWithReferenceIssue.firstOrNull()?.let {
            arrayOf(it.leftPropertyFqName, it.rightPropertyFqName, it.startLine)
        })
    }
}