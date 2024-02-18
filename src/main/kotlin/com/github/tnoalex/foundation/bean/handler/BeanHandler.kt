package com.github.tnoalex.foundation.bean.handler

abstract class BeanHandler {

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
        } else {
            nextHandler = null
        }
    }

    protected abstract fun canHandle(bean: Any): Boolean
    protected open fun doHandle(bean: Any) {}

    class DefaultBeanHandler : BeanHandler() {
        override fun canHandle(bean: Any): Boolean {
            return false
        }
    }
}