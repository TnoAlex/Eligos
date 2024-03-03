package com.github.tnoalex.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintStream

/**
 * Std out err wrapper
 * The purpose of this file is to collect all system.out() and system.err() so that its logs can be managed by SL4J
 */
object StdOutErrWrapper {
    private val logger = LoggerFactory.getLogger(StdOutErrWrapper::class.java)

    fun init() {
        logger.warn("All stdOut and StdErr will be redirect to here")
        val printStreamForOut = createLoggingWrapper(System.out, false)
        val printStreamForErr = createLoggingWrapper(System.out, true)
        System.setOut(printStreamForOut)
        System.setErr(printStreamForErr)
    }

    private fun getCallerLogger(): Logger {
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.size < 6) {
            return logger
        }
        return LoggerFactory.getLogger(stackTrace[5].className)
    }

    private fun createLoggingWrapper(
        printStream: PrintStream,
        isErr: Boolean
    ): PrintStream {
        return object : PrintStream(printStream) {
            override fun print(string: String?) {
                if (!isErr) {
                    getCallerLogger().info(string)
                } else {
                    getCallerLogger().error(string)
                }
            }

            override fun print(b: Boolean) {
                if (!isErr) {
                    getCallerLogger().info(b.toString())
                } else {
                    getCallerLogger().error(b.toString())
                }
            }

            override fun print(c: Char) {
                if (!isErr) {
                    getCallerLogger().info(c.toString())
                } else {
                    getCallerLogger().error(c.toString())
                }
            }

            override fun print(i: Int) {
                if (!isErr) {
                    getCallerLogger().info(i.toString())
                } else {
                    getCallerLogger().error(i.toString())
                }
            }

            override fun print(l: Long) {
                if (!isErr) {
                    getCallerLogger().info(l.toString())
                } else {
                    getCallerLogger().error(l.toString())
                }
            }

            override fun print(f: Float) {
                if (!isErr) {
                    getCallerLogger().info(f.toString())
                } else {
                    getCallerLogger().error(f.toString())
                }
            }

            override fun print(d: Double) {
                if (!isErr) {
                    getCallerLogger().info(d.toString())
                } else {
                    getCallerLogger().error(d.toString())
                }
            }

            override fun print(x: CharArray) {
                if (!isErr) {
                    getCallerLogger().debug(String(x))
                } else {
                    getCallerLogger().error(String(x))
                }
            }

            override fun print(obj: Any?) {
                if (!isErr) {
                    getCallerLogger().info(obj?.toString())
                } else {
                    getCallerLogger().error(obj?.toString())
                }
            }
        }
    }
}