package com.github.tnoalex.foundation.bean.container

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.BeanNameManager
import com.github.tnoalex.foundation.bean.BeanScope

object SimpleSingletonBeanContainer : BeanContainer {
    private val container: HashMap<String, Any> = HashMap(16)
    override val scope: BeanScope
        get() = BeanScope.Singleton
    override val containerId: Int
        get() = 0

    override fun addBean(beanName: String, bean: Any) {
        BeanNameManager.recordBean(beanName, bean::class.java)
        container[beanName] = bean
    }

    override fun removeBean(beanName: String): Any? {
        if (!container.containsKey(beanName))
            return null
        ApplicationContext.beanPreRemoveHandler.handle(container[beanName]!!)
        val bean = container.remove(beanName)
        if (bean != null) {
            BeanNameManager.removeBeanName(beanName, bean::class.java)
            ApplicationContext.beanAfterRemoveHandler.handle(bean)
        }
        return bean
    }

    override fun removeBean(beanType: Class<*>): List<Any> {
        val it = container.iterator()
        val removed = ArrayList<Any>()
        while (it.hasNext()) {
            val entity = it.next()
            if (beanType.isAssignableFrom(entity.value::class.java)) {
                ApplicationContext.beanPreRemoveHandler.handle(entity.value)
                BeanNameManager.removeBeanName(entity.key, entity.value.javaClass)
                removed.add(entity.value)
                it.remove()
                ApplicationContext.beanAfterRemoveHandler.handle(entity.value)
            }
        }
        return removed
    }

    override fun getBean(beanName: String): Any? {
        return container[beanName]
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getBean(beanType: Class<T>): List<T> {
        val list = ArrayList<T>()
        container.forEach { (_, v) ->
            if (beanType.isAssignableFrom(v.javaClass))
                list.add(v as T)
        }
        return list
    }

    override fun visitBeans(visitor: (String, Any) -> Unit) {
        container.forEach { (k, v) -> visitor(k, v) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getExactBean(beanType: Class<T>): T? {
        return container.values.firstOrNull { it.javaClass == beanType } as T?
    }
}