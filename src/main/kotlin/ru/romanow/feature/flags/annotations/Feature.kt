/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Feature(
    val value: String,
    val defaultValue: String = "<empty>"
)
