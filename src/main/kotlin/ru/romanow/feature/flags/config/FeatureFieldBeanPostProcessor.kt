/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.config

import org.springframework.beans.PropertyValues
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.DependencyDescriptor
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
import org.springframework.core.env.Environment
import org.springframework.util.ReflectionUtils
import ru.romanow.feature.flags.annotations.Feature
import ru.romanow.feature.flags.models.BeanBinding
import ru.romanow.feature.flags.models.FieldBinding
import ru.romanow.feature.flags.models.PendingUpdate
import java.lang.ref.WeakReference
import java.lang.reflect.Modifier.isFinal
import java.lang.reflect.Modifier.isStatic

class FeatureFieldBeanPostProcessor(private val environment: Environment) :
    InstantiationAwareBeanPostProcessor,
    DestructionAwareBeanPostProcessor,
    BeanFactoryAware {

    private lateinit var beanFactory: ConfigurableListableBeanFactory
    private val bindings = mutableListOf<BeanBinding>()

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory as ConfigurableListableBeanFactory
    }

    @Synchronized
    override fun postProcessProperties(pvs: PropertyValues, bean: Any, beanName: String): PropertyValues {
        val fields = mutableListOf<FieldBinding>()
        ReflectionUtils.doWithFields(bean.javaClass) { field ->
            val feature = field.getAnnotation(Feature::class.java)
            val value = field.getAnnotation(Value::class.java)
            if (feature != null) {
                require(!isFinal(field.modifiers) && !isStatic(field.modifiers)) {
                    "@Feature field '${field.declaringClass.name}.${field.name}' must be mutable and non-static"
                }
                require(feature.value.isNotBlank()) {
                    "@Feature field '${field.declaringClass.name}.${field.name}' must specify a non-blank feature"
                }
                require(value == null) {
                    "Field '${field.declaringClass.name}.${field.name}' must not declare both @Feature and @Value"
                }

                ReflectionUtils.makeAccessible(field)
                fields += FieldBinding(field, feature, field.get(bean))
            } else if (value != null && value.value.contains($$"${features.")) {
                require(!isFinal(field.modifiers) && !isStatic(field.modifiers)) {
                    "@Value field '${field.declaringClass.name}.${field.name}' must be mutable and non-static"
                }

                ReflectionUtils.makeAccessible(field)
                fields += FieldBinding(field, null, null)
            }
        }
        if (fields.isNotEmpty()) {
            fields.filter { it.feature != null }
                .map { PendingUpdate(bean, it.field, resolve(it)) }
                .forEach { it.apply() }
            bindings.removeAll { it.bean.get() == null || it.bean.get() === bean }
            bindings += BeanBinding(WeakReference(bean), fields)
        }
        return pvs
    }

    @Synchronized
    fun refresh() {
        bindings.removeAll { it.bean.get() == null }
        val updates = bindings
            .flatMap { binding ->
                val bean = binding.bean.get() ?: return@flatMap emptyList()
                binding.fields.map { PendingUpdate(bean, it.field, resolve(it)) }
            }
        updates.forEach { it.apply() }
    }

    @Synchronized
    override fun postProcessBeforeDestruction(bean: Any, beanName: String) {
        bindings.removeAll { it.bean.get() == null || it.bean.get() === bean }
    }

    @Synchronized
    override fun requiresDestruction(bean: Any): Boolean = bindings.any { it.bean.get() === bean }

    private fun resolve(binding: FieldBinding): Any? {
        val feature = binding.feature
            ?: return beanFactory.resolveDependency(DependencyDescriptor(binding.field, true), null)
        val value = environment.getProperty("features.${feature.value}")
            ?: feature.defaultValue.takeUnless { it == "<empty>" }
            ?: return binding.initialValue
        return beanFactory.typeConverter.convertIfNecessary(value, binding.field.type, binding.field)
    }
}
