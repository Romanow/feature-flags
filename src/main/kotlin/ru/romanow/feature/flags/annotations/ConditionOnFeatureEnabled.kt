/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.annotations

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS
import ru.romanow.feature.flags.config.FeatureScope.Companion.FEATURE_SCOPE

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnFeatureEnabledCondition::class)
@Scope(scopeName = FEATURE_SCOPE, proxyMode = TARGET_CLASS)
annotation class ConditionOnFeatureEnabled(val feature: String, val expected: String = "true")
