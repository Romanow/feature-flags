/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.annotations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnProperty(prefix = "features", matchIfMissing = false)
annotation class ConditionOnFeatureEnabled(

    @get:AliasFor(value = "name", annotation = ConditionalOnProperty::class)
    val name: String,

    @get:AliasFor(value = "havingValue", annotation = ConditionalOnProperty::class)
    val expected: String = "true",
)
