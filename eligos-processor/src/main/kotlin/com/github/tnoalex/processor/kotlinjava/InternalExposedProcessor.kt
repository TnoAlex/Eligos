package com.github.tnoalex.processor.kotlinjava

import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.elements.jvm.java.JavaClassElement
import com.github.tnoalex.elements.jvm.java.JavaElement
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.ImproperInternalExposedIssue
import com.github.tnoalex.processor.PsiProcessorWithContext
import com.github.tnoalex.utils.getJavaClassElementByQualifiedName
import com.github.tnoalex.utils.getKotlinClassElementByQualifiedName
import com.github.tnoalex.utils.getTargetTypeParent
import depends.deptypes.DependencyType
import depends.entity.Entity
import depends.entity.FileEntity
import depends.entity.KotlinTypeEntity
import depends.entity.TypeEntity

@Component
class InternalExposedProcessor : PsiProcessorWithContext() {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    private fun process(event: EntityRepoFinishedEvent) {
        val issues = getInternalConcretization()
        context.reportIssues(issues)
    }

    private fun getInternalConcretization(): ArrayList<ImproperInternalExposedIssue> {
        val relations = getDependencyPairs()

        val issues = ArrayList<ImproperInternalExposedIssue>()
        relations.forEach { (k, v) ->
            val javaFile = getParentFileEntity(k.first)
            val kotlinFile = getParentFileEntity(k.second)

            val javaClassElement =
                getJavaClassElementByQualifiedName(context.getFileElement(javaFile), k.first.qualifiedName)
                    ?: throw RuntimeException("Can not found java element called ${k.first.qualifiedName} in file $javaFile")
            if (!isExpandVisibility(javaClassElement)) return@forEach
            val kotlinClassElement =
                getKotlinClassElementByQualifiedName(context.getFileElement(kotlinFile), k.second.qualifiedName)
                    ?: throw RuntimeException("Can not found java element called ${k.second.qualifiedName} in file $kotlinFile")

            if (kotlinClassElement.isInternal()) {
                issues.add(
                    ImproperInternalExposedIssue(
                        hashSetOf(javaFile, kotlinFile),
                        javaClassElement, kotlinClassElement, v
                    )
                )
            }
        }
        return issues
    }

    private fun getDependencyPairs(): HashMap<Pair<TypeEntity, KotlinTypeEntity>, String> {
        val repoIterator = context.getRepo().entityIterator()
        val relations = HashMap<Pair<TypeEntity, KotlinTypeEntity>, String>()
        repoIterator.forEach loop@{
            if (it is TypeEntity && it !is KotlinTypeEntity) {
                it.relations.forEach { r ->
                    if (r.type != DependencyType.IMPLEMENT && r.type != DependencyType.INHERIT) return@forEach
                    if (r.entity !is KotlinTypeEntity) return@forEach
                    relations[Pair(it, r.entity as KotlinTypeEntity)] = r.type
                }
            }
        }
        return relations
    }


    private fun isExpandVisibility(classElement: JavaClassElement): Boolean {
        var element: JavaElement = classElement
        while (element.parent !is FileElement) {
            if (!element.isPublicVisibility()) {
                return false
            }
            element = element.parent as JavaElement
        }
        return element.isPublicVisibility()
    }

    private fun getParentFileEntity(entity: Entity): String {
        val fileEntity = entity.getTargetTypeParent(FileEntity::class.java)
            ?: throw RuntimeException("Unexpected entity structures")

        return fileEntity.qualifiedName
    }
}