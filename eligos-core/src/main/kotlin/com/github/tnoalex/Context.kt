package com.github.tnoalex

import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.issues.Issue
import depends.deptypes.DependencyType
import depends.entity.repo.EntityRepo
import depends.generator.DependencyGenerator
import depends.generator.FileDependencyGenerator
import depends.generator.FunctionDependencyGenerator
import depends.generator.StructureDependencyGenerator
import depends.matrix.core.DependencyMatrix
import multilang.depends.util.file.path.EmptyFilenameWritter
import java.util.*
import kotlin.reflect.KClass

@Component
class Context {
    private lateinit var dependsRepo: EntityRepo

    private val dependsMatrices: EnumMap<AnalysisHierarchyEnum, DependencyMatrix> =
        EnumMap(AnalysisHierarchyEnum::class.java)

    private val issues = LinkedHashSet<Issue>()

    private val fileElements = LinkedList<FileElement>()

    fun getFileElement(fileName: String): FileElement {
        return fileElements.first { it.elementName == fileName }
    }

    fun getLastElement(): FileElement {
        return fileElements.last()
    }

    fun addFileElement(element: FileElement) {
        fileElements.add(element)
    }


    fun setDependsRepo(repo: EntityRepo) {
        dependsRepo = repo
        generateDependencyMatrices()
        EventBus.post(EntityRepoFinishedEvent(this))
    }

    fun getIssues() = issues
    fun reportIssue(issue: Issue) {
        issues.add(issue)
    }

    fun reportIssues(issue: List<Issue>) {
        issue.forEach {
            reportIssue(it)
        }
    }

    fun getIssuesByType(clazz: KClass<out Issue>): List<Issue> {
        return issues.filter { it::class == clazz }
    }

    fun getRepo(): EntityRepo {
        if (!this::dependsRepo.isInitialized) {
            throw RuntimeException("Repo is not initialized yet")
        }
        return dependsRepo
    }

    fun getDependencyMatrix(type: AnalysisHierarchyEnum) = dependsMatrices[type]

    private fun generateDependencyMatrices() {
        val dependencyType = DependencyType.allDependencies()
        AnalysisHierarchyEnum.entries.forEach {
            var dependencyGenerator: DependencyGenerator? = null
            when (it) {
                AnalysisHierarchyEnum.FILE -> {
                    dependencyGenerator = FileDependencyGenerator()
                }

                AnalysisHierarchyEnum.CLASS -> {
//                    if (language.lowercase() == "kotlin") {
//                           dependencyGenerator = ClassDependencyGenerator()
//                    } else {
//                        logger.warn("Non-kotlin projects are not supported at this time")
//                    }
                }

                AnalysisHierarchyEnum.STRUCTURE -> {
                    dependencyGenerator = StructureDependencyGenerator()
                }

                AnalysisHierarchyEnum.METHOD -> {
                    dependencyGenerator = FunctionDependencyGenerator()
                }
            }
            dependencyGenerator?.let { dep ->
                dep.setOutputSelfDependencies(false)
                dep.setFilenameRewritter(EmptyFilenameWritter())
                dependsMatrices[it] = dep.identifyDependencies(dependsRepo, dependencyType)
            }
        }
    }
}