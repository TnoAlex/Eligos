package com.github.tnoalex.foundation.bean.handler

/**
 * invoked before each bean register into container
 */
abstract class BeanPreRegisterHandler : BeanHandler()

/**
 * invoked after each bean register into container
 */
abstract class BeanPostRegisterHandler : BeanHandler()

/**
 * invoked after all bean register into container
 */
abstract class BeansAfterRegisterHandler : BeanHandler()

/**
 * invoked before each bean removed
 */
abstract class BeanPreRemoveHandler:BeanHandler()

/**
 * invoked after each bean removed
 */
abstract class BeanAfterRemoveHandler:BeanHandler()