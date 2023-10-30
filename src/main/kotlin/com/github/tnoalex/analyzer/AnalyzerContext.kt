package com.github.tnoalex.analyzer

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum.*
import com.github.tnoalex.formatter.IFormatter
import depends.entity.repo.EntityRepo
import depends.extractor.LangProcessorRegistration
import depends.generator.ClassDependencyGenerator
import depends.generator.FileDependencyGenerator
import depends.generator.FunctionDependencyGenerator
import depends.generator.StructureDependencyGenerator
import depends.matrix.core.DependencyMatrix
import depends.relations.BindingResolver
import depends.relations.IBindingResolver
import depends.relations.RelationCounter
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.collections.HashMap

class AnalyzerContext(
    private val language: String,
    private val sourcePath: File,
    private val outputName: String,
    private val outputPath: File,
    private val formatter: IFormatter?
) {
    private var entityRepo: EntityRepo? = null
    private var dependencyMatrices: HashMap<AnalysisHierarchyEnum, DependencyMatrix> = HashMap()

    init {
        generateDependencyMatrices()
    }

    fun getDependencyMatrix(type: AnalysisHierarchyEnum) = dependencyMatrices[type]

    private fun generateDependencyMatrices() {
        val langProcessor = LangProcessorRegistration.getRegistry().getProcessorOf(language)
        val bindingResolver: IBindingResolver =
            BindingResolver(langProcessor, false, true)

        logger.info("Starting Generate dependency matrices")
        entityRepo = langProcessor.buildDependencies(sourcePath.path, arrayOf(), bindingResolver)
        RelationCounter(entityRepo, langProcessor, bindingResolver).computeRelations()
        AnalysisHierarchyEnum.entries.forEach {
            when (it) {
                FILE -> {
                    dependencyMatrices[it] = FileDependencyGenerator().identifyDependencies(entityRepo, emptyList())
                }

                CLASS -> {
                    if (language.lowercase() == "kotlin") {
                        dependencyMatrices[it] =
                            ClassDependencyGenerator().identifyDependencies(entityRepo, emptyList())
                    } else {
                        logger.warn("Non-kotlin projects are not supported at this time")
                    }
                }

                STRUCTURE -> {
                    dependencyMatrices[it] =
                        StructureDependencyGenerator().identifyDependencies(entityRepo, emptyList())
                }

                METHOD -> {
                    dependencyMatrices[it] = FunctionDependencyGenerator().identifyDependencies(entityRepo, emptyList())
                }
            }
        }
        logger.info("Generate dependency matrices success")
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AnalyzerContext::class.java)
    }
}