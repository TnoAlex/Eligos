package com.github.tnoalex.foundation.bean.container

import com.github.tnoalex.utils.scanEntries
import org.reflections.scanners.Scanners

class DefaultBeanContainerScanner : BeanContainerScanner {
    override fun scanBeanContainers(): List<Class<out BeanContainer>>? {
        return scanEntries(Scanners.SubTypes).getSubTypesOf(BeanContainer::class.java)?.toList()
    }
}