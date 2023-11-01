package com.github.tnoalex.entity.enums

/**
 * Used to describe how information on the edge of an adjacency table is handled when it encounters a duplicate edge
 *
 * [APPEND] new info will append after the old one
 *
 * [REPLACE] new info will replace the old one
 *
 * [DISCARD] New information will be discarded
 */
enum class DuplicateEdgeStrategy {
    APPEND,
    REPLACE,
    DISCARD
}