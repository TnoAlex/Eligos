package com.github.tnoalex.processor.java

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.java.MissingNullabilityAnnotationIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*

@Component
@Suitable(LaunchEnvironment.CLI)
class MissingNullabilityAnnotationProcessor: IssueProcessor {
    override val severity: Severity = Severity.SUGGESTION

    companion object {
        val annos = listOf("NonNull", "Nullable")
    }

    @EventListener(filterClazz = [PsiJavaFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile as PsiJavaFile
        psiFile.accept(object: JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.annotations.all {
                        it.qualifiedName?.split(".")?.last() !in annos
                    }) {
                    context.reportIssue(
                        MissingNullabilityAnnotationIssue(
                            psiFile.filePath,
                            method.containingClass?.qualifiedName,
                            method.name,
                            method.startLine
                        )
                    )
                }
                super.visitMethod(method)
            }
        })
    }
}