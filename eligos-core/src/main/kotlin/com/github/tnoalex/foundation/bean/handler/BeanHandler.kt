package com.github.tnoalex.foundation.bean.handler

abstract class BeanHandler {
    abstract val handlerOrder: Int
    private var nextHandler: BeanHandler? = null
    fun handle(bean: Any) {
        if (canHandle(bean)) {
            doHandle(bean)
        }
        nextHandler?.handle(bean)
    }

    fun addHandler(handler: BeanHandler) {
        if (nextHandler == null)
            nextHandler = handler
        else {
            nextHandler!!.addHandler(handler)
        }
    }

    fun removeHandlers() {
        if (nextHandler != null) {
            nextHandler!!.removeHandlers()
        }
        dispose()
        nextHandler = null
    }

    protected abstract fun canHandle(bean: Any): Boolean
    protected open fun doHandle(bean: Any) {}
    protected open fun dispose(){}

    companion object {
        const val LOWEST_ORDER = -1;
        const val HIGHEST_ORDER = Int.MAX_VALUE;
    }

    class DefaultBeanHandler : BeanHandler() {
        override val handlerOrder: Int
            get() = LOWEST_ORDER

        override fun canHandle(bean: Any): Boolean {
            return false
        }
    }
}