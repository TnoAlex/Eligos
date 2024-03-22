package com.github.tnoalex.processor.common

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.github.tnoalex.processor.utils.startLine
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.GabowStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
class CircularReferencesProcessor : PsiProcessor {
    private var dependencyGraph = newEmptyGraph()
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    @EventListener
    fun process(psiFile: PsiFile) {
        when (psiFile) {
            is PsiJavaFile -> handleJavaFile(psiFile)
            is KtFile -> handleKtFile(psiFile)
        }
    }

    @EventListener
    fun resultGeneration(event: AllFileParsedEvent) {
        val sccAlg = GabowStrongConnectivityInspector(dependencyGraph)
        val scc = sccAlg.stronglyConnectedComponents.filter { it.vertexSet().size > 1 } // Outliers also is scc
        scc.forEach {
            val subGraph = newEmptyGraph()
            it.vertexSet().forEach { v -> subGraph.addVertex(v) }
            it.edgeSet().forEach { e -> subGraph.addEdge(it.getEdgeSource(e), it.getEdgeTarget(e)) }
            context.reportIssue(CircularReferencesIssue(it.vertexSet().toHashSet(), subGraph))
        }
        dependencyGraph = newEmptyGraph()
    }

    private fun handleKtFile(ktFile: KtFile) {
        val fileName = ktFile.virtualFilePath
        dependencyGraph.addVertex(fileName)
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective()) return super.visitReferenceExpression(expression)
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null)
                    return super.visitReferenceExpression(expression)
                expression.referenceExpressionSelfOrInChildren().forEach {
                    try {
                        it.references.forEach { ref ->
                            ref.resolve()?.let { r -> resolveRef(r, fileName) }
                        }
                    } catch (e: NullPointerException) {
                        logger.warn(
                            "Can not resolve reference in file ${expression.containingFile.virtualFile.path}," +
                                    "line ${expression.startLine}"
                        )
                    }
                }
                super.visitReferenceExpression(expression)
            }
        })
    }

    private fun handleJavaFile(javaFile: PsiJavaFile) {
        val fileName = javaFile.virtualFile.path
        dependencyGraph.addVertex(fileName)
        javaFile.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                try {
                    reference.resolve()?.let {
                        resolveRef(it, fileName)
                    }
                } catch (e: IllegalArgumentException) {
                    logger.warn("Can not resolve reference in file ${reference.containingFile.virtualFile.path},line ${reference.startLine}")
                }
                super.visitReferenceElement(reference)
            }
        })
    }

    private fun resolveRef(providerElement: PsiElement, consumeFile: String) {
        if (PsiTreeUtil.getParentOfType(providerElement, KtDecompiledFile::class.java) != null) return
        if (PsiTreeUtil.getParentOfType(providerElement, PsiCompiledElement::class.java) != null) return
        val srcElement =
            if (providerElement is KtLightElement<*, *>) providerElement.kotlinOrigin!! else providerElement
        PsiTreeUtil.getParentOfType(srcElement, PsiJavaFile::class.java)?.let {
            it.virtualFile ?: return
            addDependency(it.virtualFile.path, consumeFile)
            return
        }
        PsiTreeUtil.getParentOfType(srcElement, KtFile::class.java)?.let {
            it.virtualFile ?: return
            addDependency(it.virtualFilePath, consumeFile)
            return
        }
    }

    private fun addDependency(providerFile: String, consumeFile: String) {
        if (providerFile == consumeFile) return
        dependencyGraph.addVertex(providerFile)
        dependencyGraph.addEdge(providerFile, consumeFile)
    }

    private fun newEmptyGraph(): Graph<String, DefaultEdge> {
        return GraphTypeBuilder.directed<String, DefaultEdge>().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultEdge::class.java).weighted(false).buildGraph()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CircularReferencesProcessor::class.java)
    }
}