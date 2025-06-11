/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.annotations

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OnDefaultFeatureCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val attributes = metadata.getAnnotationAttributes(DefaultFeatureImplementation::class.qualifiedName!!)
        val featureName = attributes?.get("feature") as String? ?: return false
        val feature = context.environment.getProperty("features.$featureName", String::class.java)
        return feature == null || !hasEnabledClasses(featureName, feature)
    }

    private fun hasEnabledClasses(featureName: String, feature: String): Boolean {
        val classes = findAnnotatedClasses(ConditionOnFeatureEnabled::class.java)
        return classes.any {
            val annotation = it.getAnnotation(ConditionOnFeatureEnabled::class.java)
            return annotation != null && annotation.feature == featureName && feature == annotation.expected
        }
    }

    private fun findAnnotatedClasses(annotation: Class<out Annotation>): Set<Class<*>> {
        val reflections = Reflections(ConfigurationBuilder().addScanners(Scanners.TypesAnnotated))
        return reflections.getTypesAnnotatedWith(annotation)
    }
}
