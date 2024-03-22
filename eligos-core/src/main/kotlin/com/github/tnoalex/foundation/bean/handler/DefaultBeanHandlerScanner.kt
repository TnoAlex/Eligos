package com.github.tnoalex.foundation.bean.handler

import com.github.tnoalex.utils.scanEntries
import org.reflections.scanners.Scanners


class DefaultBeanHandlerScanner : BeanHandlerScanner {
    override fun scanBeanHandler(): List<Class<out BeanHandler>>? {
        return scanEntries(Scanners.SubTypes).getSubTypesOf(BeanHandler::class.java)?.toList()
    }
}