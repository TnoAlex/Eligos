package com.github.tnoalex.processor.common.java

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.SubProcessor
import com.github.tnoalex.processor.common.TooManyParametersProcessor
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter

@Component
class JavaTooManyParametersProcessor : SubProcessor {
    private lateinit var shareSpace: TooManyParametersProcessor.TooManyParametersShareSpace
    override fun process(psiFile: PsiFile, shareSpace: ShareSpace) {
        this.shareSpace = shareSpace as TooManyParametersProcessor.TooManyParametersShareSpace
        psiFile.accept(javaFunctionVisitor())
    }

    private fun javaFunctionVisitor(): JavaRecursiveElementVisitor {
        return object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.parameters.size >= shareSpace.shareArity) {
                    with(method) {
                        shareSpace.shareIssues.add(
                            ExcessiveParamsIssue(
                                containingFile.virtualFile.path, name,
                                method.parameters.map { (it as PsiParameter).text },
                                method.startLine,
                                parameters.size
                            )
                        )
                    }
                }
                super.visitMethod(method)
            }
        }
    }
}