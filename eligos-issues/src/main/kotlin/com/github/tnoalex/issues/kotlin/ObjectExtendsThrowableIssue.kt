package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec

class ObjectExtendsThrowableIssue(affectedFile: String, val objectFqName: String) :
    Issue(AnalysisHierarchyEnum.CLASS, hashSetOf(affectedFile), "Object Extends Throwable") {

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["objectFqName"] = objectFqName
        return rawMap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ObjectExtendsThrowableIssue

        return objectFqName == other.objectFqName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + objectFqName.hashCode()
        return result
    }
}