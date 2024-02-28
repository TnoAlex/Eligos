/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtDeclarationSymbol

public abstract class KtMultiplatformInfoProvider : KtAnalysisSessionComponent() {
    public abstract fun getExpectForActual(actual: KtDeclarationSymbol): KtDeclarationSymbol?
}

public interface KtMultiplatformInfoProviderMixin : KtAnalysisSessionMixIn {

    /**
     * Gives expect symbol for the actual one if it is available.
     **/
    public fun KtDeclarationSymbol.getExpectForActual(): KtDeclarationSymbol? =
        withValidityAssertion { analysisSession.multiplatformInfoProvider.getExpectForActual(this) }

}