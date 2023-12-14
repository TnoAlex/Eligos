package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.elements.jvm.java.JavaClassElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinClassElement

class ImproperInternalConcretizationIssue(
    affectedFiles: HashSet<String>,
    val javaClassElement: JavaClassElement,
    val kotlinClassElement: KotlinClassElement,
    val relation: String
) : Issue(AnalysisHierarchyEnum.CLASS, affectedFiles) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaClassElement.hashCode()
        result = 31 * result + kotlinClassElement.hashCode()
        return result
    }
}