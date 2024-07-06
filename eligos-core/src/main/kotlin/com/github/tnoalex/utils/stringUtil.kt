package com.github.tnoalex.utils

fun String.equalsIgnoreCase(str: String): Boolean {
    return this.lowercase() == str.lowercase()
}