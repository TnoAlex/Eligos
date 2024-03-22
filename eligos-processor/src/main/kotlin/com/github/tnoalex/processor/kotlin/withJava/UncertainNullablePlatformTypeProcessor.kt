package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformExpressionUsageIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformTypeInPropertyIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.bindingContext
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import org.jetbrains.kotlin.cfg.containingDeclarationForPseudocode
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.smartcasts.Nullability
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.util.javaslang.getOrNull
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class UncertainNullablePlatformTypeProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")
    val dataFlowValueFactory = ApplicationContext.getBean(DataFlowValueFactory::class.java).first()
    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(kotlinPropertyVisitor)
    }

    private val kotlinPropertyVisitor = object : KtTreeVisitorVoid() {
        override fun visitExpression(expression: KtExpression) {
            val bindingContext = expression.bindingContext
            val expectedType =
                bindingContext[BindingContext.EXPECTED_EXPRESSION_TYPE, expression] ?: return super.visitExpression(
                    expression
                )
            if (expectedType.isNullable() || expectedType.isFlexibleRecursive()) {
                return super.visitExpression(expression)
            }
            val type = bindingContext.getType(expression) ?: return super.visitExpression(expression)
            if (type.isDynamic()) return super.visitExpression(expression)
            val typeInfo = bindingContext[BindingContext.EXPRESSION_TYPE_INFO, expression]
            val dataFlowInfo = typeInfo?.dataFlowInfo ?: return super.visitExpression(expression)
            val completeNullabilityInfo = dataFlowInfo.completeNullabilityInfo
            val declarationDescriptor =
                expression.containingDeclarationForPseudocode?.resolveToDescriptorIfAny()
                    ?: return super.visitExpression(expression)
            val dataFlowValue =
                dataFlowValueFactory.createDataFlowValue(expression, type, bindingContext, declarationDescriptor)
            val nullabilities = completeNullabilityInfo[dataFlowValue].getOrNull()
            if (nullabilities != Nullability.NOT_NULL && type.isFlexibleRecursive()) {
                context.reportIssue(
                    UncertainNullablePlatformExpressionUsageIssue(
                        expression.containingFile.virtualFile.path,
                        expression.parent.text!!,
                        expression.startLine,
                        expectedType.toString(),
                        type.toString(),
                        nullabilities?.toString() ?: "no smart cast"
                    )
                )
            }
            super.visitExpression(expression)
        }


        override fun visitProperty(property: KtProperty) {
            if (property.isLocal) return super.visitProperty(property)
            val descriptor = property.resolveToDescriptorIfAny() ?: let {
                logger.warn("Unknown type of ${property.text} in file ${property.containingFile.name}")
                return super.visitProperty(property)
            }
            val propertyType = descriptor.type
            // dynamic type can not be resolved
            if (propertyType.isDynamic()) return super.visitProperty(property)
            if (propertyType.isFlexibleRecursive()) {
                //found platform type
                context.reportIssue(
                    UncertainNullablePlatformTypeInPropertyIssue(
                        property.containingFile.virtualFile.path,
                        property.text,
                        property.name ?: let {
                            logger.warn("unknown property name in file ${property.containingFile.name} at line ${property.startLine}")
                            "unknown property name"
                        },
                        property.startLine,
                        propertyType.upperIfFlexible().toString(),
                        propertyType.lowerIfFlexible().toString(),
                        property.isTopLevel
                    )
                )
            }
            super.visitProperty(property)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(UncertainNullablePlatformTypeProcessor::class.java)
    }

    private fun KotlinType.isFlexibleRecursive(): Boolean {
        if (isFlexible()) return true
        return arguments.any { !it.isStarProjection && it.type.isFlexibleRecursive() }
    }
}