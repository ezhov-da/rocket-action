package ru.ezhov.rocket.action.types.todoist

import com.google.gson.Gson
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.types.todoist.model.Project
import java.util.*

private val logger = KotlinLogging.logger { }

class TodoistProjectRepository {
    @Throws(TodoistRepositoryException::class)
    fun projects(token: String): List<Project> {
        val projects: MutableList<Project> = ArrayList()
        var debugToken = ""
        if (token.length > 5) {
            debugToken = token.substring(0, 4)
        }
        logger.info { "method=projects todoistToken=$debugToken" }
        return try {
            val request: Request = Builder()
                    .url(BASE_URL.value)
                    .header("Authorization", String.format("Bearer %s", token))
                    .build()
            val client = OkHttpClient()
            val call = client.newCall(request)
            val response = call.execute()
            val code = response.code
            response.body.use { body ->
                val text = body!!.string()
                if (code == 200) {
                    val gson = Gson()
                    val projectsFromJson = gson.fromJson(text, Array<Project>::class.java)
                    if (projectsFromJson.size > 0) {
                        projects.addAll(Arrays.asList(*projectsFromJson))
                    }
                } else {
                    throw TodoistRepositoryException(
                            "Exception when get todoist projects with code=$code and text=$text")
                }
            }
            projects
        } catch (e: Exception) {
            throw TodoistRepositoryException("Exception when get todoist projects", e)
        }
    }

    companion object {
        private val TOKEN_PROPERTY = RocketActionConfigurationPropertyKey("rocket.action.toodoist.token")
        private val BASE_URL = RocketActionConfigurationPropertyKey("https://api.todoist.com/rest/v1/projects")
    }
}