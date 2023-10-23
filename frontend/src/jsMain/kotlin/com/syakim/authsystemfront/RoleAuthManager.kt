package com.syakim.authsystemfront

import com.syakim.authsystemfront.helpers.withProgress
import com.syakim.authsystemfront.model.Endpoint
import io.kvision.redux.createTypedReduxStore
import io.kvision.rest.RemoteRequestException

import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import org.w3c.dom.get
import org.w3c.dom.set

object RoleAuthManager : CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob()) {

    const val JWT_TOKEN = "jwtToken"
    const val USER = "userName"

    val appReduxStore = createTypedReduxStore(::stateReducer, AppState())

    fun initialize() {
        initializeRoutes().resolve()

        if (getFromLocalStorage(JWT_TOKEN) != null) {
            withProgress {
                try {
                    appReduxStore.dispatch(AppAction.EndpointsLoading)
                    val username = getFromLocalStorage(USER)
                    val user = Api.getUserInfo(username)
                    appReduxStore.dispatch(AppAction.Login(user))
                    appReduxStore.dispatch(AppAction.EndpointsLoaded(user.endpoints))
                    routing.navigate(View.PROFILE.url)
                } catch (e: Exception) {
                    console.log("Invalid JWT Token")
                    removeFromLocalStorage(JWT_TOKEN)
                    removeFromLocalStorage(USER)
                }
            }
        } else {
            routing.navigate(View.LOGIN.url)
        }
        afterInitialize()
    }

    private fun afterInitialize() {
        appReduxStore.dispatch(AppAction.AppLoaded)
    }

    fun login(login: String?, password: String?) {
        withProgress {
            if (login == null || password == null) {
                appReduxStore.dispatch(AppAction.LoginError(error = "Please enter both login and password"))
                return@withProgress
            }
            try {
                val (user, userAuthResponse) = Api.login(login, password)
                appReduxStore.dispatch(AppAction.Login(user))
                saveToLocalStorage(USER, login)
                saveToLocalStorage(JWT_TOKEN, userAuthResponse.jwtToken)
                routing.navigate(View.PROFILE.url)
            } catch (e: RemoteRequestException) {
                val error = parseError(e.response?.text()?.await())
                appReduxStore.dispatch(AppAction.LoginError(error = error))
            }
        }
    }

    fun loadBusinessEndpoint(businessEndpoint: Endpoint) {
        appReduxStore.dispatch(AppAction.BusinessLoading(businessEndpoint))
        withProgress {
            val statusResponse = Api.sendRoleRequest(businessEndpoint.url)
            appReduxStore.dispatch(AppAction.BusinessLoaded(statusResponse))
            routing.navigate(View.BUSINESS.url)
        }
    }

    fun loginPage() {
        appReduxStore.dispatch(AppAction.LoginPage)
    }

    fun profilePage() {
        appReduxStore.dispatch(AppAction.ProfilePage)
        val state = appReduxStore.getState()
        loadEndpoints(state.user?.login)
    }

    fun businessPage() {
        appReduxStore.dispatch(AppAction.BusinessPage)
    }

    fun logout() {
        removeFromLocalStorage(JWT_TOKEN)
        removeFromLocalStorage(USER)
        appReduxStore.dispatch(AppAction.Logout)
        routing.navigate(View.LOGIN.url)
    }

    private fun loadEndpoints(username: String?) {
        if (username != null) {
            appReduxStore.dispatch(AppAction.EndpointsLoading)
            withProgress {
                val user = Api.getUserInfo(username)
                appReduxStore.dispatch(AppAction.EndpointsLoaded(user.endpoints))
            }
        }
    }

    fun getJwtToken(): String? {
        return getFromLocalStorage(JWT_TOKEN)
    }

    private fun saveToLocalStorage(key: String, value: String) {
        localStorage[key] = value
    }

    private fun getFromLocalStorage(key: String): String? {
        return localStorage[key]
    }

    private fun removeFromLocalStorage(key: String) {
        localStorage.removeItem(key)
    }

    private fun parseError(message: String?): String {
        return message?.let {
            try {
                val json = JSON.parse<dynamic>(it)
                json.description
            } catch (e: Exception) {
                "unknown error"
            }
        } as? String ?: "unknown error"
    }
}
