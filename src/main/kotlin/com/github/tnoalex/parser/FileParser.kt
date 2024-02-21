package com.github.tnoalex.parser

import java.io.File

interface FileParser {
    fun parseFile(file: File): Any?
}