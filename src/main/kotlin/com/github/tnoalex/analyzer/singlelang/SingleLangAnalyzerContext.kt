package com.github.tnoalex.analyzer.singlelang

import com.github.tnoalex.analyzer.AbstractAnalyzerContext
import com.github.tnoalex.analyzer.AnalysisHierarchyEnum
import com.github.tnoalex.analyzer.AnalysisHierarchyEnum.*
import com.github.tnoalex.antipatterns.*
import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.algorithm.AdjacencyList
import depends.deptypes.DependencyType
import depends.entity.repo.EntityRepo
import depends.generator.DependencyGenerator
import depends.generator.FileDependencyGenerator
import depends.generator.FunctionDependencyGenerator
import depends.generator.StructureDependencyGenerator
import depends.matrix.core.DependencyMatrix
import multilang.depends.util.file.path.EmptyFilenameWritter
import org.slf4j.LoggerFactory
import java.util.*

open class SingleLangAnalyzerContext(
    val language: String,
    val formatter: IFormatter,
    val entityRepo: EntityRepo
) : AbstractAnalyzerContext() {

    private val dependencyMatrices: EnumMap<AnalysisHierarchyEnum, DependencyMatrix> =
        EnumMap(AnalysisHierarchyEnum::class.java)
    private val antiPatterns: EnumMap<AntiPatternEnum, LinkedHashSet<AntiPatternEntity>> =
        EnumMap(AntiPatternEnum::class.java)

    init {
        generateDependencyMatrices()
    }

    fun getDependencyMatrix(type: AnalysisHierarchyEnum) = dependencyMatrices[type]

    private fun generateDependencyMatrices() {
        val dependencyType = DependencyType.allDependencies()
        AnalysisHierarchyEnum.entries.forEach {
            var dependencyGenerator: DependencyGenerator? = null
            when (it) {
                FILE -> {
                    dependencyGenerator = FileDependencyGenerator()
                }

                CLASS -> {
//                    if (language.lowercase() == "kotlin") {
//                           dependencyGenerator = ClassDependencyGenerator()
//                    } else {
//                        logger.warn("Non-kotlin projects are not supported at this time")
//                    }
                }

                STRUCTURE -> {
                    dependencyGenerator = StructureDependencyGenerator()
                }

                METHOD -> {
                    dependencyGenerator = FunctionDependencyGenerator()
                }
            }
            dependencyGenerator?.let { dep ->
                dep.setOutputSelfDependencies(false)
                dep.setFilenameRewritter(EmptyFilenameWritter())
                dependencyMatrices[it] = dep.identifyDependencies(entityRepo, dependencyType)
            }
        }
        logger.info("Generate dependency matrices success")
    }

    fun foundUnusedImportPattern(affectedFilesIndexes: List<Int>) {
        val patterns = antiPatterns[AntiPatternEnum.UNUSED_IMPORT]
        val affectedFiles = affectedFilesIndexes.map { dependencyMatrices[FILE]!!.getNodeName(it) }
        if (patterns == null) {
            antiPatterns[AntiPatternEnum.UNUSED_IMPORT] =
                linkedSetOf(UnusedImportPatternEntity(affectedFiles.toHashSet(), affectedFiles[0]))
        } else {
            val pattern = patterns.filter { it.identifier == affectedFiles[0] }
            if (pattern.isEmpty()) {
                patterns.add(UnusedImportPatternEntity(affectedFiles.toHashSet(), affectedFiles[0]))
            } else {
                pattern[0].affectedFiles.addAll(affectedFiles)
                antiPatterns[AntiPatternEnum.UNUSED_IMPORT]!!.remove(pattern[0])
                antiPatterns[AntiPatternEnum.UNUSED_IMPORT]!!.add(pattern[0])
            }
        }
    }

    fun foundCircularReferences(affectedFilesIndexes: List<Int>, subReference: AdjacencyList<Int>) {
        val patterns = antiPatterns[AntiPatternEnum.CIRCULAR_REFS]
        val affectedFiles = affectedFilesIndexes.map { dependencyMatrices[FILE]!!.getNodeName(it) }
        if (patterns == null) {
            antiPatterns[AntiPatternEnum.CIRCULAR_REFS] =
                linkedSetOf(CircularRefEntity(affectedFiles.toHashSet(), subReference))
        } else {
            antiPatterns[AntiPatternEnum.CIRCULAR_REFS]!!.add(
                CircularRefEntity(
                    affectedFiles.toHashSet(),
                    subReference
                )
            )
        }
    }

    fun foundTooManyParameters(affectedFile: String, arity: Int, funcName: String) {
        val patterns = antiPatterns[AntiPatternEnum.TOO_MANY_PARAMS]
        if (patterns == null) {
            antiPatterns[AntiPatternEnum.TOO_MANY_PARAMS] =
                linkedSetOf(ExcessiveParamsEntity(affectedFile, funcName, arity))
        } else {
            antiPatterns[AntiPatternEnum.TOO_MANY_PARAMS]!!.add(
                ExcessiveParamsEntity(affectedFile, funcName, arity)
            )
        }
    }


    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SingleLangAnalyzerContext::class.java)
    }
}