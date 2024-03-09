package com.github.tnoalex

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.statistics.Statistics
import kotlin.reflect.KClass

@Component
class Context {
    private val issues = LinkedHashSet<Issue>()
    private val stats = ArrayList<Statistics>()

    fun getIssues() = issues
    fun reportIssue(issue: Issue) {
        issues.add(issue)
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