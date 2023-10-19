package com.syakim.authsystemfront

import com.syakim.authsystemfront.helpers.withProgress
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

    val appReduxStore = createTypedReduxStore(::conduitReducer, AppState())

    fun initialize() {
        initializeRoutes().resolve()

        appReduxStore.dispatch(AppAction.LoginPage)
//        if (getJwtToken() != null) {
//            withProgress {
//                try {
//                    val user = Api.user()
//                    systemStore.dispatch(ConduitAction.Login(user))
//                    saveJwtToken(user.token!!)
//                    afterInitialize(FeedType.USER)
//                } catch (e: Exception) {
//                    console.log("Invalid JWT Token")
//                    deleteJwtToken()
//                    afterInitialize(FeedType.GLOBAL)
//                }
//            }
//        } else {
            afterInitialize(FeedType.GLOBAL)
//        }
    }

    private fun afterInitialize(feedType: FeedType) {
        appReduxStore.dispatch(AppAction.AppLoaded)
        if (appReduxStore.getState().view == View.HOME) {
//            selectFeed(feedType)
//            loadTags()
        }
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
                saveJwtToken(userAuthResponse.jwtToken)
                routing.navigate(View.HOME.url)
            } catch (e: RemoteRequestException) {
                val error = parseError(e.response?.text()?.await())
                appReduxStore.dispatch(AppAction.LoginError(error = error))
            }
        }
    }

    fun loginPage() {
        appReduxStore.dispatch(AppAction.LoginPage)
    }



//    fun selectFeed(feedType: FeedType, selectedTag: String? = null, profile: User? = null) {
//        conduitStore.dispatch(ConduitAction.SelectFeed(feedType, selectedTag, profile))
//        loadArticles()
//    }

//    fun selectPage(page: Int) {
//        conduitStore.dispatch(ConduitAction.SelectPage(page))
//        loadArticles()
//    }


    fun showProfile(username: String, favorites: Boolean) {
        val feedType = if (favorites) FeedType.PROFILE_FAVORITED else FeedType.PROFILE
        appReduxStore.dispatch(AppAction.ProfilePage(feedType))
//        withProgress {
//            val user = Api.profile(username)
//            selectFeed(feedType, null, user)
//        }
    }

//    fun toggleProfileFollow(user: User) {
//        if (systemStore.getState().user != null) {
//            withProgress {
//                val changedUser = Api.profileFollow(user.username!!, !user.following!!)
//                systemStore.dispatch(ConduitAction.ProfileFollowChanged(changedUser))
//            }
//        } else {
//            routing.navigate(View.LOGIN.url)
//        }
//    }

//    private fun loadArticles() {
//        conduitStore.dispatch(ConduitAction.ArticlesLoading)
//        withProgress {
//            val state = conduitStore.getState()
//            val limit = state.pageSize
//            val offset = state.selectedPage * limit
//            val articleDto = when (state.feedType) {
//                FeedType.USER -> Api.feed(offset, limit)
//                FeedType.GLOBAL -> Api.articles(null, null, null, offset, limit)
//                FeedType.TAG -> Api.articles(state.selectedTag, null, null, offset, limit)
//                FeedType.PROFILE -> Api.articles(null, state.profile?.username, null, offset, limit)
//                FeedType.PROFILE_FAVORITED -> Api.articles(null, null, state.profile?.username, offset, limit)
//            }
//            conduitStore.dispatch(ConduitAction.ArticlesLoaded(articleDto.articles, articleDto.articlesCount))
//        }
//    }

//    private fun loadTags() {
//        conduitStore.dispatch(ConduitAction.TagsLoading)
//        withProgress {
//            val tags = Api.tags()
//            conduitStore.dispatch(ConduitAction.TagsLoaded(tags))
//        }
//    }

//    fun editorPage(slug: String? = null) {
//        if (slug == null) {
//            conduitStore.dispatch(ConduitAction.EditorPage(null))
//        } else {
//            withProgress {
//                val article = Api.article(slug)
//                conduitStore.dispatch(ConduitAction.EditorPage(article))
//            }
//        }
//    }

//    fun createArticle(title: String?, description: String?, body: String?, tags: String?) {
//        withProgress {
//            val tagList = tags?.split(" ")?.toList() ?: emptyList()
//            try {
//                val article = Api.createArticle(title, description, body, tagList)
//                routing.navigate(View.ARTICLE.url + "/" + article.slug)
//            } catch (e: RemoteRequestException) {
//                conduitStore.dispatch(
//                    ConduitAction.EditorError(
//                        Article(
//                            title = title,
//                            description = description,
//                            body = body,
//                            tagList = tagList
//                        ), parseErrors(e.response?.text()?.await())
//                    )
//                )
//            }
//        }
//    }

//    fun updateArticle(slug: String, title: String?, description: String?, body: String?, tags: String?) {
//        withProgress {
//            val tagList = tags?.split(" ")?.toList() ?: emptyList()
//            try {
//                val article = Api.updateArticle(slug, title, description, body, tagList)
//                routing.navigate(View.ARTICLE.url + "/" + article.slug)
//            } catch (e: RemoteRequestException) {
//                conduitStore.dispatch(
//                    ConduitAction.EditorError(
//                        conduitStore.getState().editedArticle!!.copy(
//                            title = title,
//                            description = description,
//                            body = body,
//                            tagList = tagList
//                        ), parseErrors(e.response?.text()?.await())
//                    )
//                )
//            }
//        }
//    }

//    fun deleteArticle(slug: String) {
//        withProgress {
//            Api.deleteArticle(slug)
//            routing.navigate(View.HOME.url)
//        }
//    }

    fun homePage() {
        appReduxStore.dispatch(AppAction.HomePage)
        val state = appReduxStore.getState()
//        if (!state.appLoading) {
//            if (state.user != null) {
//                selectFeed(FeedType.USER)
//            } else {
//                selectFeed(FeedType.GLOBAL)
//            }
//            loadTags()
//        }
    }

    fun getJwtToken(): String? {
        return localStorage[JWT_TOKEN]
    }

    private fun saveJwtToken(token: String) {
        localStorage[JWT_TOKEN] = token
    }

    private fun deleteJwtToken() {
        localStorage.removeItem(JWT_TOKEN)
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
