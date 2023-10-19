package com.syakim.authsystemfront

import io.kvision.core.StringPair
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestClient
import io.kvision.rest.call
import io.kvision.rest.post
import io.kvision.rest.requestDynamic
import io.realworld.model.*
import kotlinx.coroutines.await

object Api {

    const val API_URL = "http://localhost:8080/v0"

    private val restClient = RestClient()

    private fun authRequest(): List<StringPair> {
        return RoleAuthManager.getJwtToken()?.let {
            listOf("Authorization" to "Token $it")
        } ?: emptyList()
    }

    suspend fun login(username: String, password: String): Pair<User, AuthResponse> {
        val user = User(
            login = username,
            password = password
        )
        val response = restClient.post<AuthResponse, User>(
            "$API_URL/users/auth",
            user
        ).await()

        return user to response
    }

    suspend fun register(username: String?, email: String?, password: String?): User {
        return restClient.post<UserDto, UserDto>(
            "$API_URL/users",
            UserDto(
                User(
                    login = username,
                    email = email,
                    password = password
                )
            )
        ).await().user
    }

    suspend fun user(): User {
        return restClient.call<UserDto>(
            "$API_URL/user"
        ) { headers = ::authRequest }.await().user
    }

//    suspend fun settings(image: String?, username: String?, bio: String?, email: String?, password: String?): User {
//        return restClient.call<UserDto, UserDto>(
//            "$API_URL/user",
//            UserDto(
//                User(
//                    image = image,
//                    username = username,
//                    bio = bio,
//                    email = email,
//                    password = password
//                )
//            )
//        ) {
//            method = HttpMethod.PUT
//            headers = ::authRequest
//        }.await().user
//    }

//    suspend fun tags(): List<String> {
//        return restClient.call<TagsDto>(
//            "$API_URL/tags"
//        ).await().tags
//    }

//    suspend fun articles(
//        tag: String?,
//        author: String?,
//        favorited: String?,
//        offset: Int = 0,
//        limit: Int = 10
//    ): ArticlesDto {
//        return restClient.call<ArticlesDto, ArticlesQuery>(
//            "$API_URL/articles",
//            ArticlesQuery(tag, author, favorited, offset, limit)
//        ) { headers = ::authRequest }.await()
//    }

//    suspend fun feed(offset: Int = 0, limit: Int = 10): ArticlesDto {
//        return restClient.call<ArticlesDto, FeedQuery>(
//            "$API_URL/articles/feed",
//            FeedQuery(offset, limit)
//        ) { headers = ::authRequest }.await()
//    }

//    suspend fun article(slug: String): Article {
//        return restClient.call<ArticleDto>(
//            "$API_URL/articles/$slug"
//        ) { headers = ::authRequest }.await().article
//    }
//
//    suspend fun articleComments(slug: String): List<Comment> {
//        return restClient.call<CommentsDto>(
//            "$API_URL/articles/$slug/comments"
//        ) { headers = ::authRequest }.await().comments
//    }

//    suspend fun articleComment(slug: String, comment: String?): Comment {
//        return restClient.post<CommentDto, CommentDto>(
//            "$API_URL/articles/$slug/comments",
//            CommentDto(Comment(body = comment))
//        ) { headers = ::authRequest }.await().comment
//    }

//    suspend fun articleCommentDelete(slug: String, id: Int) {
//        restClient.requestDynamic(
//            "$API_URL/articles/$slug/comments/$id"
//        ) {
//            method = HttpMethod.DELETE
//            headers = ::authRequest
//        }.await()
//    }

//    suspend fun articleFavorite(slug: String, favorite: Boolean = true): Article {
//        return restClient.call<ArticleDto>(
//            "$API_URL/articles/$slug/favorite"
//        ) {
//            method = if (favorite) HttpMethod.POST else HttpMethod.DELETE
//            headers = ::authRequest
//        }.await().article
//    }

//    suspend fun profile(username: String): User {
//        return restClient.call<ProfileDto>(
//            "$API_URL/profiles/$username"
//        ) { headers = ::authRequest }.await().profile
//    }

//    suspend fun profileFollow(username: String, follow: Boolean = true): User {
//        return restClient.call<ProfileDto>(
//            "$API_URL/profiles/$username/follow"
//        ) {
//            method = if (follow) HttpMethod.POST else HttpMethod.DELETE
//            headers = ::authRequest
//        }.await().profile
//    }

//    suspend fun createArticle(title: String?, description: String?, body: String?, tags: List<String>): Article {
//        return restClient.post<ArticleDto, ArticleDto>(
//            "$API_URL/articles",
//            ArticleDto(
//                Article(
//                    title = title,
//                    description = description,
//                    body = body,
//                    tagList = tags
//                )
//            )
//        ) { headers = ::authRequest }.await().article
//    }

//    suspend fun updateArticle(
//        slug: String,
//        title: String?,
//        description: String?,
//        body: String?,
//        tags: List<String>
//    ): Article {
//        return restClient.call<ArticleDto, ArticleDto>(
//            "$API_URL/articles/$slug",
//            ArticleDto(
//                Article(
//                    title = title,
//                    description = description,
//                    body = body,
//                    tagList = tags
//                )
//            )
//        ) {
//            method = HttpMethod.PUT
//            headers = ::authRequest
//        }.await().article
//    }

//    suspend fun deleteArticle(slug: String) {
//        restClient.requestDynamic(
//            "$API_URL/articles/$slug",
//        ) {
//            method = HttpMethod.DELETE
//            headers = ::authRequest
//        }.await()
//    }

}
