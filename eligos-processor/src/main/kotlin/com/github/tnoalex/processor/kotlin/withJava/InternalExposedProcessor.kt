package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.processor.PsiProcessor
import com.intellij.psi.PsiFile

@Component
class InternalExposedProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")

    @EventListener
    private fun process(psiFile: PsiFile) {

    }

//    private fun getInternalConcretization(): ArrayList<ImproperInternalExposedIssue> {
//        val relations = getDependencyPairs()
//
//        val issues = ArrayList<ImproperInternalExposedIssue>()
//        relations.forEach { (k, v) ->
//
//            val javaClassElement =
//                getJavaClassElementByQualifiedName(context.getFileElement(javaFile), k.first.qualifiedName)
//                    ?: throw RuntimeException("Can not found java element called ${k.first.qualifiedName} in file $javaFile")
//            if (!isExpandVisibility(javaClassElement)) return@forEach
//            val kotlinClassElement =
//                getKotlinClassElementByQualifiedName(context.getFileElement(kotlinFile), k.second.qualifiedName)
//                    ?: throw RuntimeException("Can not found java element called ${k.second.qualifiedName} in file $kotlinFile")
//
//            if (kotlinClassElement.isInternal()) {
//                issues.add(
//                    ImproperInternalExposedIssue(
//                        hashSetOf(javaFile, kotlinFile),
//                        javaClassElement, kotlinClassElement, v
//                    )
//                )
//            }
//        }
//        return issues
//    }

//    private fun getDependencyPairs(): HashMap<Pair<TypeEntity, KotlinTypeEntity>, String> {
//        val repoIterator = context.getRepo().entityIterator()
//        val relations = HashMap<Pair<TypeEntity, KotlinTypeEntity>, String>()
//        repoIterator.forEach loop@{
//            if (it is TypeEntity && it !is KotlinTypeEntity) {
//                it.relations.forEach { r ->
//                    if (r.type != DependencyType.IMPLEMENT && r.type != DependencyType.INHERIT) return@forEach
//                    if (r.entity !is KotlinTypeEntity) return@forEach
//                    relations[Pair(it, r.entity as KotlinTypeEntity)] = r.type
//                }
//            }
//        }
//        return relations
//    }
//
//
//    private fun isExpandVisibility(classElement: JavaClassElement): Boolean {
//        var element: JavaElement = classElement
//        while (element.parent !is FileElement) {
//            if (!element.isPublicVisibility()) {
//                return false
//            }
//            element = element.parent as JavaElement
//        }
//        return element.isPublicVisibility()
//    }

}