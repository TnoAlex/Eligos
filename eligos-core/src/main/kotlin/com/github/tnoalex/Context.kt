package com.github.tnoalex

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.statistics.Statistics
import kotlin.reflect.KClass

@Component
class Context {
    /**
     * Only allowed to set before analyze
     */
    var confidenceLevel: ConfidenceLevel = ConfidenceLevel.DEFAULT
    val issues = HashSet<Issue>()
    val stats = ArrayList<Statistics>()

    fun reportIssue(issue: Issue) {
        // check confidence level that needed
        if (confidenceLevel <= issue.confidenceLevel) {
            issues.add(issue)
        }
    }

    fun reportIssues(issue: List<Issue>) {
        issue.forEach {
            reportIssue(it)
        }
    }

    fun resetContext() {
        issues.clear()
        stats.clear()
    }

    fun reportStatistics(statistics: Statistics) {
        stats.add(statistics)
    }

    fun getIssuesByType(clazz: KClass<out Issue>): List<Issue> {
        return issues.filter { it::class == clazz }
    }
}