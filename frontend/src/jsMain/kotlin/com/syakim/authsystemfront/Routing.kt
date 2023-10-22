package com.syakim.authsystemfront

import io.kvision.navigo.Navigo
import io.kvision.routing.Routing

enum class View(val url: String) {
    PROFILE("/profile"),
    LOGIN("/login"),
    BUSINESS("/business")
}

lateinit var routing: Routing

fun initializeRoutes(): Navigo {
    routing = Routing.init(null, true, "#")
    return routing
        .on(View.LOGIN.url, { _ ->
            RoleAuthManager.loginPage()
        })
        .on(View.PROFILE.url, { _ ->
            RoleAuthManager.profilePage()
        })
        .on(View.BUSINESS.url, { _ ->
            RoleAuthManager.businessPage()
        })
}