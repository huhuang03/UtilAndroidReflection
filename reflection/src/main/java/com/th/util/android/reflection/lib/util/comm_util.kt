package com.th.util.android.reflection.lib.util

import java.lang.reflect.Field
import java.lang.reflect.Method


private val fieldCache = HashMap<String, Field?>()

/**
 * Look up a method in a class and set it to accessible.
 *
 *
 * See [.findMethodBestMatch] for details. This variant
 * determines the parameter types from the classes of the given objects. For any item that is
 * `null`, the type is taken from `parameterTypes` instead.
 */
fun getStaticObjectField(clazz: Class<*>, fieldName: String): Any {
    try {
        return findField(clazz, fieldName).get(null)
    } catch (e: IllegalAccessException) {
        throw IllegalAccessError(e.message)
    } catch (e: IllegalArgumentException) {
        throw e
    }

}


private fun findField(clazz: Class<*>, fieldName: String): Field {
    val fullFieldName = clazz.name + '#'.toString() + fieldName

    if (fieldCache.containsKey(fullFieldName)) {
        return fieldCache[fullFieldName] ?: throw NoSuchFieldError(fullFieldName)
    }

    try {
        val field = findFieldRecursiveImpl(clazz, fieldName)
        field.isAccessible = true
        fieldCache[fullFieldName] = field
        return field
    } catch (e: NoSuchFieldException) {
        fieldCache[fullFieldName] = null
        throw NoSuchFieldError(fullFieldName)
    }

}


@Throws(NoSuchFieldException::class)
private fun findFieldRecursiveImpl(clazz: Class<*>, fieldName: String): Field {
    try {
        return clazz.getDeclaredField(fieldName)
    } catch (e: NoSuchFieldException) {
        while (true) {
            val superClass = clazz.superclass
            if (superClass == null || superClass == Any::class.java)
                break

            try {
                return superClass.getDeclaredField(fieldName)
            } catch (ignored: NoSuchFieldException) {
            }

        }
        throw e
    }
}


