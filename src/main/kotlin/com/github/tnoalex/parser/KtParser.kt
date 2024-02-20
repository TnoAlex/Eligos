package com.github.tnoalex.parser

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.text.StringUtilRt
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText

class KtParser(coreEnvironment: KotlinCoreEnvironment) {
    private val psiFactory = KtPsiFactory(coreEnvironment.project)

    fun parseKotlinFile(path: Path): KtFile {
        require(path.isRegularFile()) { "Given sub path ($path) should be a regular file!" }
        val normalizedAbsolutePath = path.absolute().normalize()
        val content = path.readText()

        return psiFactory.createPhysicalFile(
            normalizedAbsolutePath.name,
            StringUtilRt.convertLineSeparators(content)
        )
    }
}