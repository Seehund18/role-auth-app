package com.syakim.authsystemfront.layout

import com.syakim.authsystemfront.AppState
import com.syakim.authsystemfront.RoleAuthManager
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

fun Container.homePage(state: AppState) {
    div(className = "home-page") {
        div(className = "banner") {
            div(className = "container") {
                h1("Welcome back ${state.user?.login}", className = "logo-font")
                p("Here you can see endpoints available to you")
            }
        }
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-9") {
                    if (state.endpointsLoading) {
                        div("Loading available endpoints...", className = "article-preview")
                    } else if (state.availableEndpoints.isNotEmpty()) {
                        state.availableEndpoints.forEach {
                            div(className = "article-preview") {
                                h1(content = it.url)
                                p(content = it.description)
                            }
                        }
                    } else {
                        div("No endpoints here...", className = "article-preview")
                    }
                }
            }
            button("Click here to logout.", style = ButtonStyle.OUTLINEDANGER).onClick {
                RoleAuthManager.logout()
            }
        }
    }
}
