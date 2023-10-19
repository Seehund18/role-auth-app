package com.syakim.authsystemfront

import io.kvision.navigo.Navigo
import io.kvision.routing.Routing
import kotlin.js.RegExp

enum class View(val url: String) {
    HOME("/"),

    PROFILE("/@"),
    LOGIN("/login"),
//    REGISTER("/register"),
}

lateinit var routing: Routing

fun initializeRoutes(): Navigo {
    routing = Routing.init(null, true, "#")
    return routing
        .on(View.HOME.url, { _ ->
            RoleAuthManager.homePage()
        })
        .on(RegExp("^${View.PROFILE.url}([^/]+)$"), { username ->
            RoleAuthManager.showProfile(username, false)
        })
        .on(RegExp("^${View.PROFILE.url}([^/]+)/favorites$"), { username ->
            RoleAuthManager.showProfile(username, true)
        })
        .on(View.LOGIN.url, { _ ->
            RoleAuthManager.loginPage()
        })
//        .on(View.REGISTER.url, { _ ->
//        ConduitManager.registerPage()
//    })
}