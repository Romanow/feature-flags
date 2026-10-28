/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.models

import ru.romanow.feature.flags.annotations.Feature
import java.lang.ref.WeakReference
import java.lang.reflect.Field

internal data class BeanBinding(val bean: WeakReference<Any>, val fields: List<FieldBinding>)

internal data class FieldBinding(val field: Field, val feature: Feature?, val initialValue: Any?)

internal data class PendingUpdate(val bean: Any, val field: Field, val value: Any?) {
    fun apply() {
        field.set(bean, value)
    }
}
