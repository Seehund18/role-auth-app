package com.test.authsystem.aop

import com.test.authsystem.constants.SystemRoles

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authorized(val minRole: SystemRoles)