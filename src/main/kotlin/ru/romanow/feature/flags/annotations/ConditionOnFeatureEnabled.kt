/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConditionOnFeatureEnabled(val feature: String, val expected: String = "true")
