package ru.ezhov.rocket.action.types.todoist;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.types.todoist.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TodoistTaskRepository {
    public static final String TOKEN_PROPERTY = "rocket.action.toodoist.token";
    private static final Logger LOGGER = Logger.getLogger(TodoistTaskRepository.class.getName());
    private static final String URL_ALL_TASKS_GET = "https://api.todoist.com/rest/v1/tasks";
    private static final String URL_CHANGE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s";
    private static final String URL_CLOSE_TASK_POST = "https://api.todoist.com/rest/v1/tasks/%s/close";
    private static final String URL_DELETE_TASK_DELETE = "https://api.todoist.com/rest/v1/tasks/%s";

    public List<Task> tasks(RocketActionSettings settings) throws TodoistRepositoryException {
        String token = getToken(settings);
        List<Task> projects = new ArrayList<>();

        try {
            Request request = new Request.Builder()
                    .url(URL_ALL_TASKS_GET)
                    .header("Authorization", String.format("Bearer %s", token))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            Response response = call.execute();

            int code = response.code();
            try (final ResponseBody body = response.body()) {
                String text = body.string();
                if (code == 200) {
                    Gson gson = new Gson();
                    final Task[] tasksFromJson = gson.fromJson(text, Task[].class);
                    if (tasksFromJson.length > 0) {
                        projects.addAll(Arrays.asList(tasksFromJson));
                    }
                } else {
                    throw new TodoistRepositoryException(
                            "Exception when get todoist tasks with code=" + code + " and text=" + text);
                }
            }

            return projects;
        } catch (Exception e) {
            throw new TodoistRepositoryException("Exception when get todoist projects", e);
        }
    }

    private String getToken(RocketActionSettings settings) {
        String token = settings.settings().get(TodoistRocketAction.TOKEN);
        if (token == null || "".equals(token)) {
            token = System.getProperty(TOKEN_PROPERTY, "");
        }

        String debugToken = "";
        if (token.length() > 5) {
            debugToken = token.substring(0, 4);
        }
        LOGGER.log(Level.INFO, "todoistToken={0}", debugToken);

        return token;
    }

    public void change(String taskId, String content, RocketActionSettings settings) throws TodoistRepositoryException {
        String token = getToken(settings);
        try {
            RequestBody requstBody = RequestBody.create(new Gson().toJson(new Content(content)), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(String.format(URL_CHANGE_TASK_POST, taskId))
                    .post(requstBody)
                    .header("Authorization", String.format("Bearer %s", token))
                    .header("X-Request-Id", UUID.randomUUID().toString())
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            Response response = call.execute();

            int code = response.code();
            if (code != 204) {
                try (final ResponseBody body = response.body()) {
                    String text = body.string();
                    throw new TodoistRepositoryException(
                            "Exception when change todoist task with code=" + code + " and text=" + text
                    );
                }
            }
        } catch (Exception e) {
            throw new TodoistRepositoryException("Exception when change todoist task", e);
        }
    }

    public void close(String taskId, RocketActionSettings settings) throws TodoistRepositoryException {
        String token = getToken(settings);
        try {
            Request request = new Request.Builder()
                    .url(String.format(URL_CLOSE_TASK_POST, taskId))
                    .post(RequestBody.create(new byte[]{}))
                    .header("Authorization", String.format("Bearer %s", token))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            Response response = call.execute();

            int code = response.code();
            if (code != 204) {
                try (final ResponseBody body = response.body()) {
                    String text = body.string();
                    throw new TodoistRepositoryException(
                            "Exception when close todoist task with code=" + code + " and text=" + text
                    );
                }
            }
        } catch (Exception e) {
            throw new TodoistRepositoryException("Exception when change todoist task", e);
        }
    }

    public void delete(String taskId, RocketActionSettings settings) throws TodoistRepositoryException {
        String token = getToken(settings);
        try {
            Request request = new Request.Builder()
                    .url(String.format(URL_DELETE_TASK_DELETE, taskId))
                    .delete()
                    .header("Authorization", String.format("Bearer %s", token))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            Response response = call.execute();

            int code = response.code();
            if (code != 204) {
                try (final ResponseBody body = response.body()) {
                    String text = body.string();
                    throw new TodoistRepositoryException(
                            "Exception when delete todoist task with code=" + code + " and text=" + text
                    );
                }
            }
        } catch (Exception e) {
            throw new TodoistRepositoryException("Exception when change todoist task", e);
        }
    }

    private class Content {
        private String content;

        public Content(String content) {
            this.content = content;
        }
    }
}
