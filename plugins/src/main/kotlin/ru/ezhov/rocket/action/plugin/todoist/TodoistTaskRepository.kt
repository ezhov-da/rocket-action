package ru.ezhov.rocket.action.plugin.todoist

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.ezhov.rocket.action.plugin.todoist.model.Task
import java.util.*

class TodoistTaskRepository {
    @Throws(TodoistRepositoryException::class)
    fun tasks(token: String): List<Task> {
        val projects: MutableList<Task> = ArrayList()
        return try {
            val request: Request = Builder()
                .url(URL_ALL_TASKS_GET)
                .header("Authorization", "Bearer $token")
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
                    if (tasksFromJson.isNotEmpty()) {
                        projects.addAll(listOf(*tasksFromJson))
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
    }

    @Throws(TodoistRepositoryException::class)
    fun change(token: String, taskId: String?, content: String) {
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
    fun close(token: String, taskId: String?) {
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
    fun delete(token: String, taskId: String?) {
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
        private const val URL_ALL_TASKS_GET = "https://api.todoist.com/rest/v1/tasks"
        private const val URL_CHANGE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s"
        private const val URL_CLOSE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s/close"
        private const val URL_DELETE_TASK_DELETE = "https://api.todoist.com/rest/v1/tasks/%s"
    }
}