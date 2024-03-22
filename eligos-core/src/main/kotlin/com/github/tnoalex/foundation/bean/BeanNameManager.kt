package com.github.tnoalex.foundation.bean


object BeanNameManager {
    private val beanNames = HashSet<String>()
    private val beanTypes = HashMap<Class<*>, Int>()
    fun containsBean(beanName: String): Boolean {
        return beanNames.contains(beanName)
    }

    fun containsBean(beanType: Class<*>): Boolean {
        return beanTypes.keys.any { beanType.isAssignableFrom(it) }
    }

    fun recordBean(beanName: String, bean: Class<*>) {
        if (beanNames.contains(beanName)) {
            throw RuntimeException("Duplicate bean names: $beanName")
        }
        beanNames.add(beanName)
        beanTypes.getOrPut(bean) { 1 }.plus(1)
    }

    fun removeBeanName(beanName: String, bean: Class<*>) {
        beanNames.remove(beanName)
        beanTypes.compute(bean) { _, v -> v?.minus(1) }
        if (beanTypes[bean] == 0) {
            beanTypes.remove(bean)
        }
    }
}