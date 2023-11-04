package com.github.tnoalex.utils

import java.security.MessageDigest

private val CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
fun encodeBySHA1(str: String): String {
    try {
        val messageDigest = MessageDigest.getInstance("SHA1")
        messageDigest.update(str.toByteArray())
        val digest = messageDigest.digest()
        val sb = java.lang.StringBuilder()
        val len = digest.size
        for (j in 0..<len) {
            sb.append(CHARS[digest[j].toInt() shr 4 and 15])
            sb.append(CHARS[digest[j].toInt() and 15])
        }
        return sb.toString()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}



