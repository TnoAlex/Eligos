package com.github.tnoalex.foundation.bean.container

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

    override fun removeBean(beanName: String): Boolean {
        val bean = container.remove(beanName)
        if (bean != null) {
            BeanNameManager.removeBeanName(beanName, bean::class.java)
        }
        return bean != null
    }

    override fun removeBean(beanType: Class<*>): Boolean {
        val it = container.iterator()
        while (it.hasNext()) {
            val entity = it.next()
            if (entity.value.javaClass == beanType) {
                BeanNameManager.removeBeanName(entity.key, entity.value.javaClass)
                it.remove()
                return true
            }
        }
        return false
    }

    override fun getBean(beanName: String): Any? {
        return container[beanName]
    }

    override fun getBean(beanType: Class<*>): List<Any> {
        val list = ArrayList<Any>()
        container.forEach { (_, v) ->
            if (beanType.isAssignableFrom(v.javaClass))
                list.add(v)
        }
        return list
    }
}