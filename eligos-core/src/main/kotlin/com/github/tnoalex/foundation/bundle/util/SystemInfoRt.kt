package com.github.tnoalex.foundation.bundle.util

object SystemInfoRt {
    private val OS_NAME: String
    private val OS_VERSION: String

    init {
        var name = System.getProperty("os.name")
        var version = System.getProperty("os.version").lowercase()

        if (name.startsWith("Windows") && name.matches("Windows \\d+".toRegex())) {
            // for whatever reason, JRE reports "Windows 11" as a name and "10.0" as a version on Windows 11
            try {
                val version2 = name.substring("Windows".length + 1) + ".0"
                if (version2.toFloat() > version.toFloat()) {
                    version = version2
                }
            } catch (ignored: NumberFormatException) {
            }
            name = "Windows"
        }

        OS_NAME = name
        OS_VERSION = version
    }

    private val _OS_NAME = OS_NAME.lowercase()
    val isWindows: Boolean = _OS_NAME.startsWith("windows")
    val isMac: Boolean = _OS_NAME.startsWith("mac")
    val isLinux: Boolean = _OS_NAME.startsWith("linux")
    val isFreeBSD: Boolean = _OS_NAME.startsWith("freebsd")
    val isSolaris: Boolean = _OS_NAME.startsWith("sunos")
    val isUnix: Boolean = !isWindows
    val isXWindow: Boolean = isUnix && !isMac

    val isJBSystemMenu: Boolean = isMac && System.getProperty("jbScreenMenuBar.enabled", "true").toBoolean()

    val isFileSystemCaseSensitive: Boolean =
        isUnix && !isMac || "true".equals(System.getProperty("idea.case.sensitive.fs"), ignoreCase = true)
}
