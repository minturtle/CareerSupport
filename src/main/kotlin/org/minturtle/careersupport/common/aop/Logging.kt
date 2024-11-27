package org.minturtle.careersupport.common.aop

import java.lang.annotation.ElementType


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class Logging
