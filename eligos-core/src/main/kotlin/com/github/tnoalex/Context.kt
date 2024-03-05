package com.github.tnoalex


import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.Issue
import java.util.*
import kotlin.reflect.KClass

@Component
class Context {
    private val issues = LinkedHashSet<Issue>()


    fun getIssues() = issues
    fun reportIssue(issue: Issue) {
        issues.add(issue)
    }

    fun reportIssues(issue: List<Issue>) {
        issue.forEach {
            reportIssue(it)
        }
    }

    fun resetContext(){
        issues.clear()
    }

    fun getIssuesByType(clazz: KClass<out Issue>): List<Issue> {
        return issues.filter { it::class == clazz }
    }
}