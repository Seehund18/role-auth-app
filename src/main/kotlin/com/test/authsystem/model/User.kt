package com.test.authsystem.model

import java.time.LocalDate

data class User(val login: String,
                val email: String,
                val password: String,
                val birthday: LocalDate)