package com.syakim.authsystemfront.layout

import com.syakim.authsystemfront.RoleAuthManager
import com.syakim.authsystemfront.AppState
import io.kvision.core.Container
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.form.text.TextInput
import io.kvision.form.text.textInput
import io.kvision.html.ButtonType
import io.kvision.html.InputType
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.fieldset
import io.kvision.html.h1

fun Container.loginPage(state: AppState) {
    div(className = "auth-page") {
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-6 offset-md-3 col-xs-12") {
                    h1("Sign in", className = "text-xs-center")
                    if (!state.loginError.isNullOrEmpty()) {
                        div(state.loginError, className = "error-messages")
                    }
                    lateinit var loginInput: TextInput
                    lateinit var passwordInput: TextInput
                    form {
                        fieldset(className = "form-group") {
                            loginInput =
                                textInput(type = InputType.TEXT, className = "form-control form-control-lg") {
                                    placeholder = "Login"
                                }
                        }
                        fieldset(className = "form-group") {
                            passwordInput =
                                textInput(type = InputType.PASSWORD, className = "form-control form-control-lg") {
                                    placeholder = "Password"
                                }
                        }
                        button(
                            "Sign in",
                            type = ButtonType.SUBMIT,
                            className = "btn-lg pull-xs-right"
                        )
                    }.onEvent {
                        submit = { ev ->
                            ev.preventDefault()
                            RoleAuthManager.login(loginInput.value, passwordInput.value)
                        }
                    }
                }
            }
        }
    }
}