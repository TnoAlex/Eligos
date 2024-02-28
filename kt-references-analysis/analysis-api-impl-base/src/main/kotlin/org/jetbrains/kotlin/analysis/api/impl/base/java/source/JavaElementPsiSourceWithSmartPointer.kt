/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.java.source

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementPsiSource
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementSourceFactory

internal class JavaElementPsiSourceWithSmartPointer<PSI : PsiElement>(
    val pointer: SmartPsiElementPointer<PSI>,
    override val factory: JavaElementSourceFactory,
) : JavaElementPsiSource<PSI>() {

    // is used only for the purposes of equals/hashCode to avoid underlying PCE
    private val originalPsi: PSI = psi

    override val psi: PSI
        get() {
            return pointer.element
                ?: error("Cannot restore a PsiElement from $pointer")
        }

    override fun equals(other: Any?): Boolean {
        return if (other === this) true else other is JavaElementPsiSourceWithSmartPointer<*> && originalPsi == other.originalPsi
    }

    override fun hashCode(): Int = originalPsi.hashCode()
}

