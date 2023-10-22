package com.syakim.authsystemfront

import com.syakim.authsystemfront.model.User
import io.kvision.redux.RAction
import com.syakim.authsystemfront.model.Endpoint
import com.syakim.authsystemfront.model.StatusResponse

data class AppState(
    val appLoading: Boolean = true,
    val view: View = View.LOGIN,
    val user: User? = null,
    val availableEndpoints: List<Endpoint> = emptyList(),
    val endpointsLoading: Boolean = false,
    val businessEndpointLoading: Boolean = false,
    val businessEndpoint: Endpoint? = null,
    val businessEndpointResponse: StatusResponse? = null,
    val loginError: String? = null,
)

sealed class AppAction : RAction {
    object AppLoaded : AppAction()

    object ProfilePage : AppAction()
    object LoginPage : AppAction()
    object BusinessPage : AppAction()

    data class Login(val user: User) : AppAction()
    data class LoginError(val error: String) : AppAction()

    object EndpointsLoading : AppAction()
    data class EndpointsLoaded(val endpoints: List<Endpoint>) : AppAction()

    data class BusinessLoading(val endpoint: Endpoint) : AppAction()
    data class BusinessLoaded(val endpointResponse: StatusResponse) : AppAction()

    object Logout : AppAction()
}

fun stateReducer(state: AppState, action: AppAction): AppState = when (action) {
    is AppAction.AppLoaded -> {
        state.copy(appLoading = false)
    }
    is AppAction.ProfilePage -> {
        state.copy(view = View.PROFILE)
    }
    is AppAction.LoginPage -> {
        state.copy(view = View.LOGIN, loginError = null)
    }
    is AppAction.BusinessPage -> {
        state.copy(view = View.BUSINESS)
    }
    is AppAction.Login -> {
        state.copy(user = action.user)
    }
    is AppAction.LoginError -> {
        state.copy(user = null, loginError = action.error)
    }
    is AppAction.EndpointsLoading -> {
        state.copy(endpointsLoading = true)
    }
    is AppAction.EndpointsLoaded -> {
        state.copy(endpointsLoading = false, availableEndpoints = action.endpoints)
    }
    is AppAction.BusinessLoading -> {
        state.copy(businessEndpointLoading = true,
            businessEndpoint = action.endpoint,
            businessEndpointResponse = null)
    }
    is AppAction.BusinessLoaded -> {
        state.copy(businessEndpointLoading = false,
            businessEndpointResponse = action.endpointResponse)
    }
    is AppAction.Logout -> {
        AppState(appLoading = false)
    }
}
