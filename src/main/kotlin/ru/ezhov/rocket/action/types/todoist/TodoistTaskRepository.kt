package ru.ezhov.rocket.action.types.todoist

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.types.todoist.model.Task
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class TodoistTaskRepository {
    @Throws(TodoistRepositoryException::class)
    fun tasks(settings: RocketActionSettings): List<Task> =
            getToken(settings)?.let { token ->
                val projects: MutableList<Task> = ArrayList()
                return try {
                    val request: Request = Builder()
                            .url(URL_ALL_TASKS_GET)
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
                            val tasksFromJson = gson.fromJson(text, Array<Task>::class.java)
                            if (tasksFromJson.size > 0) {
                                projects.addAll(Arrays.asList(*tasksFromJson))
                            }
                        } else {
                            throw TodoistRepositoryException(
                                    "Exception when get todoist tasks with code=$code and text=$text")
                        }
                    }
                    projects
                } catch (e: Exception) {
                    throw TodoistRepositoryException("Exception when get todoist projects", e)
                }
            } ?: emptyList()

    private fun getToken(settings: RocketActionSettings): String? {
        var token = settings.settings()[TodoistRocketAction.TOKEN]
        if (token == null || "" == token) {
            token = System.getProperty(TOKEN_PROPERTY, "")
        }
        var debugToken = ""
        if (token!!.length > 5) {
            debugToken = token.substring(0, 4)
        }
        LOGGER.log(Level.INFO, "todoistToken={0}", debugToken)
        return token.takeIf { it.isNotEmpty() }
    }

    @Throws(TodoistRepositoryException::class)
    fun change(taskId: String?, content: String, settings: RocketActionSettings) {
        val token = getToken(settings)
        try {
            val requestBody: RequestBody =
                    Gson().toJson(Content(content)).toRequestBody("application/json".toMediaType())
            val request: Request = Builder()
                    .url(String.format(URL_CHANGE_TASK_POST, taskId))
                    .post(requestBody)
                    .header("Authorization", String.format("Bearer %s", token))
                    .header("X-Request-Id", UUID.randomUUID().toString())
                    .build()
            val client = OkHttpClient()
            val call = client.newCall(request)
            val response = call.execute()
            val code = response.code
            if (code != 204) {
                response.body.use { body ->
                    val text = body!!.string()
                    throw TodoistRepositoryException(
                            "Exception when change todoist task with code=$code and text=$text"
                    )
                }
            }
        } catch (e: Exception) {
            throw TodoistRepositoryException("Exception when change todoist task", e)
        }
    }

    @Throws(TodoistRepositoryException::class)
    fun close(taskId: String?, settings: RocketActionSettings) {
        val token = getToken(settings)
        try {
            val request: Request = Builder()
                    .url(String.format(URL_CLOSE_TASK_POST, taskId))
                    .post(byteArrayOf().toRequestBody("application/json".toMediaType()))
                    .header("Authorization", String.format("Bearer %s", token))
                    .build()
            val client = OkHttpClient()
            val call = client.newCall(request)
            val response = call.execute()
            val code = response.code
            if (code != 204) {
                response.body.use { body ->
                    val text = body!!.string()
                    throw TodoistRepositoryException(
                            "Exception when close todoist task with code=$code and text=$text"
                    )
                }
            }
        } catch (e: Exception) {
            throw TodoistRepositoryException("Exception when change todoist task", e)
        }
    }

    @Throws(TodoistRepositoryException::class)
    fun delete(taskId: String?, settings: RocketActionSettings) {
        val token = getToken(settings)
        try {
            val request: Request = Builder()
                    .url(String.format(URL_DELETE_TASK_DELETE, taskId))
                    .delete()
                    .header("Authorization", String.format("Bearer %s", token))
                    .build()
            val client = OkHttpClient()
            val call = client.newCall(request)
            val response = call.execute()
            val code = response.code
            if (code != 204) {
                response.body.use { body ->
                    val text = body!!.string()
                    throw TodoistRepositoryException(
                            "Exception when delete todoist task with code=$code and text=$text"
                    )
                }
            }
        } catch (e: Exception) {
            throw TodoistRepositoryException("Exception when change todoist task", e)
        }
    }

    private inner class Content(private val content: String)
    companion object {
        const val TOKEN_PROPERTY = "rocket.action.toodoist.token"
        private val LOGGER = Logger.getLogger(TodoistTaskRepository::class.java.name)
        private const val URL_ALL_TASKS_GET = "https://api.todoist.com/rest/v1/tasks"
        private const val URL_CHANGE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s"
        private const val URL_CLOSE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s/close"
        private const val URL_DELETE_TASK_DELETE = "https://api.todoist.com/rest/v1/tasks/%s"
    }
}