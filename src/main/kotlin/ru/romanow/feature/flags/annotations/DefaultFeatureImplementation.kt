/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.annotations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnProperty(prefix = "features", havingValue = "false", matchIfMissing = true)
annotation class DefaultFeatureImplementation(

    @get:AliasFor(annotation = ConditionalOnProperty::class)
    val name: String
)
