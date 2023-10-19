package com.test.authsystem.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class NotBlankPassword(
    val message: String = "Not correct password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordValidator : ConstraintValidator<NotBlankPassword, CharArray> {
    override fun isValid(value: CharArray?, context: ConstraintValidatorContext?): Boolean {
        return value != null && !checkArrayIsBlank(value)
    }

    private fun checkArrayIsBlank(charArray: CharArray): Boolean {
        for (letter in charArray) {
            if (letter != ' ') {
                return false
            }
        }
        return true
    }
}