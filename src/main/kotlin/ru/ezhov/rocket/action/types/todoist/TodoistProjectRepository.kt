package ru.ezhov.rocket.action.types.todoist

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.types.todoist.model.Project
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class TodoistProjectRepository {
    @Throws(TodoistRepositoryException::class)
    fun projects(settings: RocketActionSettings): List<Project> {
        val projects: MutableList<Project> = ArrayList()
        var token = settings.settings()[TodoistRocketAction.TOKEN]
        if (token == null || "" == token) {
            token = System.getProperty(TOKEN_PROPERTY, "")
        }
        var debugToken = ""
        if (token!!.length > 5) {
            debugToken = token.substring(0, 4)
        }
        LOGGER.log(Level.INFO, "method=projects todoistToken={0}", debugToken)
        return try {
            val request: Request = Builder()
                    .url(BASE_URL)
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
        const val TOKEN_PROPERTY = "rocket.action.toodoist.token"
        private val LOGGER = Logger.getLogger(TodoistProjectRepository::class.java.name)
        private const val BASE_URL = "https://api.todoist.com/rest/v1/projects"
    }
}