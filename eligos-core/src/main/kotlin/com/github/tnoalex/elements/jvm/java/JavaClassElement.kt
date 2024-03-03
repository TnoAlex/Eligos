package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaClassElement(
    className: String?,
    classStartLine: Int,
    classStopLine: Int,
    parent: AbstractElement?,
    annotations: LinkedList<String>,
    modifiers: LinkedList<String>,
    private val packageName: String?,
    val isInterface: Boolean,
    var anonymousType: String? = null,
) : JavaElement(className, classStartLine, classStopLine, parent, annotations, modifiers) {
    val qualifiedName: String
        get() {
            return if (!isAnonymous()) {
                if (parent is JavaClassElement)
                    "${(parent as JavaClassElement).qualifiedName}.$elementName"
                else
                    "$packageName.$elementName"
            } else {
                (parent as JavaClassElement).qualifiedName + "." + anonymousType
            }
        }

    fun isAnonymous(): Boolean {
        return elementName == null
    }
}