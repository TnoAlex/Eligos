package com.github.tnoalex.utils

import com.github.tnoalex.elements.FileElement
import com.github.tnoalex.elements.jvm.java.JavaClassElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinClassElement

fun getJavaClassElementByQualifiedName(fileElement: FileElement, qualifiedName: String): JavaClassElement? {
    var classElement: JavaClassElement? = null
    fileElement.accept {
        if (it is JavaClassElement) {
            if (it.qualifiedName == qualifiedName) {
                classElement = it
                return@accept
            }
        }
    }
    return classElement
}

fun getKotlinClassElementByQualifiedName(fileElement: FileElement, qualifiedName: String): KotlinClassElement? {
    var classElement: KotlinClassElement? = null
    fileElement.accept {
        if (it is KotlinClassElement) {
            if (it.qualifiedName == qualifiedName) {
                classElement = it
                return@accept
            }
        }
    }
    return classElement
}

