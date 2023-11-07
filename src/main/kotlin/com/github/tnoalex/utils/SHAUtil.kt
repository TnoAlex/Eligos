package com.github.tnoalex.utils

import java.security.MessageDigest

private val CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
fun encodeBySHA1ToString(str: String): String {
    val sb = java.lang.StringBuilder()
    val digest = encodeBySHA1(str)
    val len = digest.size
    for (j in 0..<len) {
        sb.append(CHARS[digest[j].toInt() shr 4 and 15])
        sb.append(CHARS[digest[j].toInt() and 15])
    }
    return sb.toString()
}

fun encodeBySHA1ToLong(str: String): Long {
    val digest = encodeBySHA1(str)
    var res: Long = 0
    for (i in digest) {
        res = (res + i.toInt() % Long.MAX_VALUE) % Long.MAX_VALUE
    }
    return res
}

private fun encodeBySHA1(str: String): ByteArray {
    try {
        val messageDigest = MessageDigest.getInstance("SHA1")
        messageDigest.update(str.toByteArray())
        return messageDigest.digest()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}