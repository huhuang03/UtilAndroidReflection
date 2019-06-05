package com.th.util.android.reflection.lib.util

import external.org.apache.commons.lang3.ClassUtils
import external.org.apache.commons.lang3.reflect.MemberUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier


private val methodCache = HashMap<String, Method?>()


fun callStaticMethod(clazz: Class<*>, methodName: String, vararg args: Any): Any {
    try {
        return findMethodBestMatch(clazz, methodName, *args).invoke(null, *args)
    } catch (e: IllegalAccessException) {
        // should not happen
        throw IllegalAccessError(e.message)
    } catch (e: IllegalArgumentException) {
        throw e
    } catch (e: InvocationTargetException) {
        throw InvocationTargetError(e.cause)
    }

}

private fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg args: Any): Method {
    return findMethodBestMatch(clazz, methodName, *getParameterTypes(*args))
}


private fun findMethodBestMatch(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>?): Method {
    val fullMethodName = clazz.name + '#'.toString() + methodName + getParametersString(*parameterTypes) + "#bestmatch"


    if (methodCache.containsKey(fullMethodName)) {
        return methodCache[fullMethodName] ?: throw NoSuchMethodError(fullMethodName)
    }

    try {
        val method = findMethodExact(clazz, methodName, *parameterTypes)
        methodCache[fullMethodName] = method
        return method
    } catch (ignored: NoSuchMethodError) {
    }

    var bestMatch: Method? = null
    var clz: Class<*>? = clazz
    var considerPrivateMethods = true
    do {
        for (method in clz!!.declaredMethods) {
            // don't consider private methods of superclasses
            if (!considerPrivateMethods && Modifier.isPrivate(method.modifiers))
                continue

            // compare name and parameters
            if (method.name == methodName && ClassUtils.isAssignable(parameterTypes, method.parameterTypes, true)) {
                // get accessible version of method
                if (bestMatch == null || MemberUtils.compareParameterTypes(
                        method.parameterTypes,
                        bestMatch.parameterTypes,
                        parameterTypes
                    ) < 0
                ) {
                    bestMatch = method
                }
            }
        }
        considerPrivateMethods = false
        clz = clz.superclass
    } while (clz != null)

    if (bestMatch != null) {
        bestMatch.isAccessible = true
        methodCache.put(fullMethodName, bestMatch)
        return bestMatch
    } else {
        val e = NoSuchMethodError(fullMethodName)
        methodCache[fullMethodName] = null
        throw e
    }
}

/**
 * Returns an array with the classes of the given objects.
 */
private fun getParameterTypes(vararg args: Any?): Array<Class<*>?> {
    val clazzes = arrayOfNulls<Class<*>>(args.size)
    for (i in args.indices) {
        clazzes[i] = if (args[i] != null) args[i]!!.javaClass else null
    }
    return clazzes
}

private fun findMethodExact(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>?): Method {
    val fullMethodName = clazz.name + '#'.toString() + methodName + getParametersString(*parameterTypes) + "#exact"

    if (methodCache.containsKey(fullMethodName)) {
        return methodCache[fullMethodName] ?: throw NoSuchMethodError(fullMethodName)
    }

    try {
        val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true
        methodCache[fullMethodName] = method
        return method
    } catch (e: NoSuchMethodException) {
        methodCache[fullMethodName] = null
        throw NoSuchMethodError(fullMethodName)
    }

}


class InvocationTargetError (cause: Throwable?) : Error(cause) {
    companion object {
        private val serialVersionUID = -1070936889459514628L
    }
}


private fun getParametersString(vararg clazzes: Class<*>?): String {
    val sb = StringBuilder("(")
    var first = true
    for (clazz in clazzes) {
        if (first)
            first = false
        else
            sb.append(",")

        if (clazz != null)
            sb.append(clazz.canonicalName)
        else
            sb.append("null")
    }
    sb.append(")")
    return sb.toString()
}

