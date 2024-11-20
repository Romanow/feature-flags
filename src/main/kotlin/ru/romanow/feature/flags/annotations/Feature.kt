/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Feature(
    val value: String,
    val default: String = "<empty>",
    val group: Group = Group.DEFAULT
)

enum class Group {
    DEFAULT,
    RELEASE,
    EXPERIMENTAL,
    PERMISSION,
    OPERATION
}
