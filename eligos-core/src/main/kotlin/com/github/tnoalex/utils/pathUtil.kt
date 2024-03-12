package com.github.tnoalex.utils

fun relativePath(basePath: String, path: String): String {
    return path.replace(basePath.replace("\\","/"), "")
}