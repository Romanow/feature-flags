/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.annotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnFeatureEnabledCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val attributes = metadata.getAnnotationAttributes(ConditionOnFeatureEnabled::class.qualifiedName!!)
        val featureName = attributes?.get("feature") as String? ?: return false
        val expected = attributes?.get("expected") as String? ?: return false
        return context.environment.getProperty("features.$featureName", String::class.java) == expected
    }
}
