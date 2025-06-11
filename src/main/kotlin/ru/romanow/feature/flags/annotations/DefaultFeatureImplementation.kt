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
@Conditional(OnDefaultFeatureCondition::class)
@Scope(scopeName = FEATURE_SCOPE, proxyMode = TARGET_CLASS)
annotation class DefaultFeatureImplementation(val feature: String)
