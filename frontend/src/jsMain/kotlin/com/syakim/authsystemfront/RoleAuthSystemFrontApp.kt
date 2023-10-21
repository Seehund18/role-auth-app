package com.syakim.authsystemfront

import com.syakim.authsystemfront.layout.homePage
import com.syakim.authsystemfront.layout.loginPage
import io.kvision.Application
import io.kvision.CoreModule
import io.kvision.html.main
import io.kvision.module
import io.kvision.pace.Pace
import io.kvision.pace.PaceOptions
import io.kvision.panel.root
import io.kvision.startApplication
import io.kvision.state.bind


class RoleAuthSystemFrontApp : Application() {

    override fun start() {
        Pace.init(io.kvision.require("pace-progressbar/themes/green/pace-theme-bounce.css"))
        Pace.setOptions(PaceOptions(manual = true))
        RoleAuthManager.initialize()
        root("kvapp") {
            main().bind(RoleAuthManager.appReduxStore) { state ->
                if (!state.appLoading) {
                    when (state.view) {
                        View.PROFILE -> {
                            homePage(state)
                        }
                        View.LOGIN -> {
                            loginPage(state)
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    startApplication(::RoleAuthSystemFrontApp, module.hot, CoreModule)
}