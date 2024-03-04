package com.github.tnoalex.processor.common

import com.github.tnoalex.Context
import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.CircularReferencesIssue
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.GabowStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

@Component
class CircularReferencesProcessor : PsiProcessor {
    private var dependencyGraph = newEmptyGraph()

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
            ApplicationContext.getExactBean(Context::class.java)!!
                .reportIssue(CircularReferencesIssue(it.vertexSet().toHashSet(), subGraph))
        }
        dependencyGraph = newEmptyGraph()
    }

    private fun handleKtFile(ktFile: KtFile) {
        val fileName = ktFile.virtualFilePath
        dependencyGraph.addVertex(fileName)
        ktFile.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective()) return
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null) return
                expression.referenceExpression()?.run {
                    references.forEach {
                        it.resolve()?.let { r -> resolveRef(r, fileName) }
                    }
                }
            }
        })
    }

    private fun handleJavaFile(javaFile: PsiJavaFile) {
        val fileName = javaFile.virtualFile.path
        dependencyGraph.addVertex(fileName)
        javaFile.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null) return
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null) return
                reference.resolve()?.let {
                    resolveRef(it, fileName)
                }
            }
        })
    }

    private fun resolveRef(providerElement: PsiElement, consumeFile: String) {
        if (PsiTreeUtil.getParentOfType(providerElement, KtDecompiledFile::class.java) != null) return
        if (PsiTreeUtil.getParentOfType(providerElement, PsiCompiledElement::class.java) != null) return
        val srcElement =
            if (providerElement is KtLightElement<*, *>) providerElement.kotlinOrigin!! else providerElement
        PsiTreeUtil.getParentOfType(srcElement, PsiJavaFile::class.java)?.let {
            addDependency(it.virtualFile.path, consumeFile)
            return
        }
        PsiTreeUtil.getParentOfType(srcElement, KtFile::class.java)?.let {
            addDependency(it.virtualFilePath, consumeFile)
            return
        }
        throw RuntimeException("Can not find parent file of element with context : ${providerElement.text}")
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
}