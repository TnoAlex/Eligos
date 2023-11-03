package com.github.tnoalex.analyzer

import com.github.tnoalex.entity.AntiPatternEntity
import com.github.tnoalex.entity.UnusedImportPatternEntity
import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum.*
import com.github.tnoalex.entity.enums.AntiPatternEnum
import com.github.tnoalex.formatter.IFormatter
import depends.LangRegister
import depends.deptypes.DependencyType
import depends.entity.repo.EntityRepo
import depends.extractor.LangProcessorRegistration
import depends.generator.DependencyGenerator
import depends.generator.FileDependencyGenerator
import depends.generator.FunctionDependencyGenerator
import depends.generator.StructureDependencyGenerator
import depends.matrix.core.DependencyMatrix
import depends.relations.BindingResolver
import depends.relations.IBindingResolver
import depends.relations.RelationCounter
import multilang.depends.util.file.path.EmptyFilenameWritter
import org.slf4j.LoggerFactory
import java.io.File
import java.util.EnumMap

class AnalyzerContext(
    private val language: String,
    private val sourcePath: File,
    private val outputName: String,
    private val outputPath: File,
    private val formatter: IFormatter?
) {
    private var entityRepo: EntityRepo? = null
    private val dependencyMatrices: EnumMap<AnalysisHierarchyEnum, DependencyMatrix> =
        EnumMap(AnalysisHierarchyEnum::class.java)
    private val antiPatterns: EnumMap<AntiPatternEnum, LinkedHashSet<AntiPatternEntity>> =
        EnumMap(AntiPatternEnum::class.java)

    init {
        LangRegister.register()
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
        val dependencyType = DependencyType.allDependencies()
        AnalysisHierarchyEnum.entries.forEach {
            var dependencyGenerator: DependencyGenerator? = null
            when (it) {
                FILE -> {
                    dependencyGenerator = FileDependencyGenerator()
                }

                CLASS -> {
//                    if (language.lowercase() == "kotlin") {
//                        dependencyMatrices[it] =
//                            ClassDependencyGenerator().identifyDependencies(entityRepo, emptyList())
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
                linkedSetOf((UnusedImportPatternEntity(affectedFiles.toHashSet(), affectedFiles[0])))
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

    fun foundCircularReferences(){

    }


    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(AnalyzerContext::class.java)
    }
}