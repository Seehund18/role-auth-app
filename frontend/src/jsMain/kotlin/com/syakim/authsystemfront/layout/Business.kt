package com.syakim.authsystemfront.layout

import com.syakim.authsystemfront.AppState
import com.syakim.authsystemfront.View
import com.syakim.authsystemfront.routing
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.h3
import io.kvision.html.p

fun Container.businessPage(state: AppState) {
    div(className = "home-page") {
        div(className = "banner") {
            div(className = "container") {
                h1("Welcome to ${state.businessEndpoint?.url}", className = "logo-font")
                p("${state.businessEndpoint?.description}")
            }
        }
        div(className = "container page") {
            div(className = "row") {
                div(className = "col-md-9") {
                    if (state.businessEndpointLoading) {
                        div("Loading info...", className = "article-preview")
                    } else {
                        div(className = "article-preview") {
                            h3("${state.businessEndpointResponse?.status}")
                            p("${state.businessEndpointResponse?.description}")
                        }
                    }
                }
            }
            button("Click here to go back", style = ButtonStyle.OUTLINEDANGER).onClick {
                routing.navigate(View.PROFILE.url)
            }
        }
    }
}