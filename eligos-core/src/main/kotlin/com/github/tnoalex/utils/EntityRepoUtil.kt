package com.github.tnoalex.utils

import depends.entity.Entity
import depends.entity.repo.EntityRepo

fun <T : Entity> EntityRepo.getEntitiesByType(clazz: Class<T>): ArrayList<Entity> {
    val entities = ArrayList<Entity>()
    entityIterator().forEach {
        if (it.javaClass == clazz) {
            entities.add(it)
        }
    }
    return entities
}