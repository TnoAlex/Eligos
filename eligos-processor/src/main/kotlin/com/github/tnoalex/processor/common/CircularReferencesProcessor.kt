package com.github.tnoalex.processor.common

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
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
        val scc = sccAlg.stronglyConnectedComponents
    }

    private fun handleKtFile(ktFile: KtFile) {

    }

    private fun handleJavaFile(javaFile: PsiJavaFile) {

    }

    private fun newEmptyGraph(): Graph<String, DefaultEdge> {
        return GraphTypeBuilder.directed<String, DefaultEdge>().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultEdge::class.java).weighted(false).buildGraph()
    }
}