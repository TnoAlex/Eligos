package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.UnusedImportIssue
import com.github.tnoalex.processor.PsiProcessor
import depends.deptypes.DependencyType
import java.util.*

@Component
class UnUsedImportProcessor : PsiProcessor {
    private val issues = LinkedList<UnusedImportIssue>()

    @EventListener
    fun process(event: EntityRepoFinishedEvent) {
        findUselessImport(event.source as Context)
        (event.source as Context).reportIssues(issues)
        issues.clear()
    }

    private fun findUselessImport(context: Context) {
        val fileDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.FILE)
        fileDependency?.run {
            dependencyPairs.filter {
                it.dependencies.size == 1 && it.dependencies.first().type.equals(DependencyType.IMPORT)
            }.forEach {
                foundUnusedImportPattern(listOf(it.from, it.to), context)
            }
        }
    }

    private fun foundUnusedImportPattern(affectedFilesIndexes: List<Int>, context: Context) {
        val affectedFiles =
            affectedFilesIndexes.map { context.getDependencyMatrix(AnalysisHierarchyEnum.FILE)!!.getNodeName(it) }
        val issue = issues.filter { it.useFile == affectedFiles[0] }
        if (issue.isEmpty()) {
            issues.add(UnusedImportIssue(affectedFiles.toHashSet(), affectedFiles[0]))
        } else {
            issue[0].affectedFiles.addAll(affectedFiles)
            issues.remove(issue[0])
            issues.add(issue[0])
        }
    }

}