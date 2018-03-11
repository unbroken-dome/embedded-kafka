package org.unbrokendome.embedded.support

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable


fun Type.resolveAsClass(): Class<*> =
        when (this) {
            is Class<*> ->
                this
            is ParameterizedType ->
                this.rawType as Class<*>
            is TypeVariable<*> ->
                this.bounds.first().resolveAsClass()
            else ->
                throw IllegalArgumentException()
        }
