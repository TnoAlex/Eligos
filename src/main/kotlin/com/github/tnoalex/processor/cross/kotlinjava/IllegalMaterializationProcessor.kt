package com.github.tnoalex.processor.cross.kotlinjava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ImproperInternalConcretizationIssue
import com.github.tnoalex.processor.AstProcessorWithContext
import com.github.tnoalex.utils.getDependencyPairByType
import com.github.tnoalex.utils.getJavaClassElementByQualifiedName
import com.github.tnoalex.utils.getKotlinClassElementByQualifiedName
import com.github.tnoalex.utils.getTargetTypeParent
import depends.deptypes.DependencyType
import depends.entity.FileEntity

class IllegalMaterializationProcessor : AstProcessorWithContext() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    override val order: Int
        get() = Int.MIN_VALUE

    @EventListener
    private fun process(event: EntityRepoFinishedEvent) {
        val issues = getInternalConcretization()
        context.reportIssues(issues)
    }

    private fun getInternalConcretization(): ArrayList<ImproperInternalConcretizationIssue> {
        val dependencyMatrix = context.getDependencyMatrix(AnalysisHierarchyEnum.STRUCTURE)!!
        val extendPairs = dependencyMatrix.getDependencyPairByType(DependencyType.INHERIT)
        val implementPairs = dependencyMatrix.getDependencyPairByType(DependencyType.IMPLEMENT)

        val issues = ArrayList<ImproperInternalConcretizationIssue>()
        (extendPairs + implementPairs).forEach {
            val from = dependencyMatrix.getNodeName(it.from)
            val to = dependencyMatrix.getNodeName(it.to)
            if (from.split("|")[1] != "Type" || to.split("|")[1] != "KotlinType") {
                return@forEach
            }
            val javaEntityName = from.split("|")[0]
            val kotlinEntityName = to.split("|")[0]
            val javaFile = getParentFileEntity(javaEntityName)
            val kotlinFile = getParentFileEntity(kotlinEntityName)

            val javaClassElement = getJavaClassElementByQualifiedName(context.getFileElement(javaFile), javaEntityName)
                ?: throw RuntimeException("Can not found java element called $javaEntityName in file $javaFile")

            val kotlinClassElement =
                getKotlinClassElementByQualifiedName(context.getFileElement(kotlinFile), kotlinEntityName)
                    ?: throw RuntimeException("Can not found java element called $kotlinEntityName in file $kotlinFile")

            if (kotlinClassElement.isInternal()) {
                issues.add(
                    ImproperInternalConcretizationIssue(
                        hashSetOf(javaFile, kotlinFile),
                        javaClassElement, kotlinClassElement, it.dependencies.map { d -> d.type }[0]
                    )
                )
            }
        }
        return issues
    }

    private fun getParentFileEntity(entityName: String): String {
        val entity = context.getRepo().getEntity(entityName)
        val fileEntity = entity.getTargetTypeParent(FileEntity::class.java)
            ?: throw RuntimeException("Unexpected entity structures")

        return fileEntity.qualifiedName
    }
}