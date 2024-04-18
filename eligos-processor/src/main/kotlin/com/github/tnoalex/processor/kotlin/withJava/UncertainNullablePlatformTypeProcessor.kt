package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformExpressionUsageIssue
import com.github.tnoalex.issues.kotlin.withJava.UncertainNullablePlatformTypeInPropertyIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.*
import org.jetbrains.kotlin.cfg.containingDeclarationForPseudocode
import org.jetbrains.kotlin.idea.references.mainReference
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
    override val severity: Severity
        get() = Severity.CODE_SMELL
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
            val expectedType = bindingContext[BindingContext.EXPECTED_EXPRESSION_TYPE, expression]
                ?: return super.visitExpression(expression)
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
                val target = expression.mainReference?.resolve()
                if (target !is KtProperty) {
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
                } else {
                    reportProperty(target)
                }
            }
            super.visitExpression(expression)
        }


        override fun visitProperty(property: KtProperty) {
            if (property.isLocal) return super.visitProperty(property)
            reportProperty(property)
            super.visitProperty(property)
        }

        private fun reportProperty(property: KtProperty) {
            val descriptor = property.resolveToDescriptorIfAny() ?: let {
                logger.typeCanNotResolveWarn("property", property)
                return
            }
            val propertyType = descriptor.type
            // dynamic type can not be resolved
            if (propertyType.isDynamic()) return
            if (propertyType.isFlexibleRecursive()) {
                //found platform type
                context.reportIssue(
                    UncertainNullablePlatformTypeInPropertyIssue(
                        property.filePath,
                        property.text,
                        property.name ?: let {
                            logger.nameCanNotResolveWarn("property", property)
                            "unknown property name"
                        },
                        property.startLine,
                        propertyType.upperIfFlexible().toString(),
                        propertyType.lowerIfFlexible().toString(),
                        property.isTopLevel,
                        property.isLocal
                    )
                )
            }
            return
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