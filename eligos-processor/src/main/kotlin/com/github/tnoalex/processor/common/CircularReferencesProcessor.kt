package com.github.tnoalex.processor.common

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.bean.inject.InjectBean
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.processor.ShareSpace
import com.github.tnoalex.processor.common.providers.CircularReferencesProcessorProvider
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtFile
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.GabowStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder

@Component
@Suitable(LaunchEnvironment.CLI)
class CircularReferencesProcessor : AbstractCommonProcessor() {
    override val severity: Severity = Severity.CODE_SMELL

    override val supportLanguage: List<Language> = listOf(JavaLanguage, KotlinLanguage)

    private var dependencyGraph = newEmptyGraph()

    @InjectBean(beanType = CircularReferencesProcessorProvider::class)
    override lateinit var processorProvider: AbstractSpecificProcessorProvider

    private val myShareSpace = CircularReferencesShareSpace()


    override fun createShearSpace(): ShareSpace = myShareSpace

    @EventListener(filterClazz = [PsiJavaFile::class, KtFile::class])
    override fun process(psiFile: PsiFile) {
        invokeSpecificProcessor(psiFile)
    }

    @EventListener
    fun resultGeneration(@Suppress("UNUSED_PARAMETER") event: AllFileParsedEvent) {
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

    private fun newEmptyGraph(): Graph<String, DefaultEdge> {
        return GraphTypeBuilder.directed<String, DefaultEdge>().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultEdge::class.java).weighted(false).buildGraph()
    }

    internal inner class CircularReferencesShareSpace : ShareSpace {
        internal val shareDependencyGraph: Graph<String, DefaultEdge>
            get() = dependencyGraph

        fun resolveRef(providerElement: PsiElement, consumeFile: String) {
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
    }
}