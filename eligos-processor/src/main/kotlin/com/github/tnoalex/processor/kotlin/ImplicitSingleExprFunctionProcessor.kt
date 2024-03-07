package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ImplicitSingleExprFunctionIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.resolveToDescriptorIfAny
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isUnit


@Component
@Suitable(LaunchEnvironment.CLI)
class ImplicitSingleExprFunctionProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin")

    @EventListener
    fun process(ktFile: KtFile) {
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (PsiTreeUtil.getChildOfType(function, KtTypeReference::class.java) != null) return
                if (PsiTreeUtil.getChildOfType(function, KtBlockExpression::class.java) != null) return
                val returnType = function.resolveToDescriptorIfAny()?.returnType ?: return
                if (returnType.unwrap().isUnit()) return
                context.reportIssue(
                    ImplicitSingleExprFunctionIssue(
                        ktFile.virtualFilePath,
                        function.fqName?.asString() ?: "unknown func",
                        function.valueParameters.map { it.name ?: "" },
                        function.startLine
                    )
                )
                super.visitNamedFunction(function)
            }
        })
    }
}