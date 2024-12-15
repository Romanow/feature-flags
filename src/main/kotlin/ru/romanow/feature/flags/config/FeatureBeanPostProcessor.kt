/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.config

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation
import ru.romanow.feature.flags.annotations.Feature
import ru.romanow.feature.flags.properties.Features
import java.lang.reflect.Field

@Component
class FeatureBeanPostProcessor(
    private val features: Features,
    private val featureBeans: FeatureBeans
) : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        if (bean.javaClass.isAnnotationPresent(DefaultFeatureImplementation::class.java) ||
            bean.javaClass.isAnnotationPresent(ConditionOnFeatureEnabled::class.java)
        ) {
            featureBeans[beanName] = bean
        } else {
            val fields = bean.javaClass.declaredFields.filter { it.isAnnotationPresent(Feature::class.java) }
            if (fields.isNotEmpty()) {
                featureBeans[beanName] = bean
                for (field in fields) {
                    val annotation = field.getAnnotation(Feature::class.java)
                    field.isAccessible = true

                    val value = if (features.containsKey(annotation.value)) {
                        if (isBoolean(field)) features[annotation.value].toBoolean() else features[annotation.value]
                    } else {
                        if (annotation.default != "<empty>") {
                            annotation.default
                        } else {
                            if (isBoolean(field)) false else throw IllegalArgumentException("Field $field has no default value")
                        }
                    }
                    field.set(bean, value)
                }
            }
        }
        return bean
    }

    private fun isBoolean(field: Field) = field.type.isAssignableFrom(Boolean::class.javaObjectType)
}

@Component
class FeatureBeans : HashMap<String, Any>()
