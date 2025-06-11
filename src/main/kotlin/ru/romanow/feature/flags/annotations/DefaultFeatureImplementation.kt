/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.annotations

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnDefaultFeatureCondition::class)
annotation class DefaultFeatureImplementation(val feature: String)
