package com.github.tnoalex.utils

import org.slf4j.LoggerFactory
import java.io.PrintStream

/**
 * Std out err wrapper
 * The purpose of this file is to collect all system.out() and system.err() so that its logs can be managed by SL4J
 */
object StdOutErrWrapper {
    private val logger = LoggerFactory.getLogger(StdOutErrWrapper::class.java)

    fun init() {
        logger.warn("All stdOut and StdErr will be redirect to hear")
        val printStreamForOut = createLoggingWrapper(System.out, false)
        val printStreamForErr = createLoggingWrapper(System.out, true)
        System.setOut(printStreamForOut)
        System.setErr(printStreamForErr)
    }

    private fun createLoggingWrapper(
        printStream: PrintStream,
        isErr: Boolean
    ): PrintStream {
        return object : PrintStream(printStream) {
            override fun print(string: String?) {
                if (!isErr) {
                    logger.info(string)
                } else {
                    logger.error(string)
                }
            }

            override fun print(b: Boolean) {
                if (!isErr) {
                    logger.info(b.toString())
                } else {
                    logger.error(b.toString())
                }
            }

            override fun print(c: Char) {
                if (!isErr) {
                    logger.info(c.toString())
                } else {
                    logger.error(c.toString())
                }
            }

            override fun print(i: Int) {
                if (!isErr) {
                    logger.info(i.toString())
                } else {
                    logger.error(i.toString())
                }
            }

            override fun print(l: Long) {
                if (!isErr) {
                    logger.info(l.toString())
                } else {
                    logger.error(l.toString())
                }
            }

            override fun print(f: Float) {
                if (!isErr) {
                    logger.info(f.toString())
                } else {
                    logger.error(f.toString())
                }
            }

            override fun print(d: Double) {
                if (!isErr) {
                    logger.info(d.toString())
                } else {
                    logger.error(d.toString())
                }
            }

            override fun print(x: CharArray) {
                if (!isErr) {
                    logger.debug(String(x))
                } else {
                    logger.error(String(x))
                }
            }

            override fun print(obj: Any?) {
                if (!isErr) {
                    logger.info(obj?.toString())
                } else {
                    logger.error(obj?.toString())
                }
            }
        }
    }
}