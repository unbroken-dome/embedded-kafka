package org.unbrokendome.embedded.support

import org.junit.platform.commons.util.AnnotationUtils
import org.junit.platform.commons.util.ReflectionUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.Optional
import kotlin.reflect.KClass


fun Class<*>.findAnnotatedMethods(annotationType: KClass<out Annotation>,
                                  traversalMode: ReflectionUtils.HierarchyTraversalMode =
                                          ReflectionUtils.HierarchyTraversalMode.TOP_DOWN): List<Method> =
        AnnotationUtils.findAnnotatedMethods(this, annotationType.java, traversalMode)


inline fun <reified A : Annotation> AnnotatedElement.findAnnotation(): Optional<A> =
        AnnotationUtils.findAnnotation(this, A::class.java)


inline fun <reified A : Annotation> AnnotatedElement.getAnnotation(): A? =
        getAnnotation(A::class.java)
