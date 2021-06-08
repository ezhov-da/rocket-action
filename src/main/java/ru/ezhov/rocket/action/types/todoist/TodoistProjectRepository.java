package ru.ezhov.rocket.action.types.todoist;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.types.todoist.model.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TodoistProjectRepository {
    public static final String TOKEN_PROPERTY = "rocket.action.toodoist.token";
    private static final Logger LOGGER = Logger.getLogger(TodoistProjectRepository.class.getName());
    private static final String BASE_URL = "https://api.todoist.com/rest/v1/projects";

    public List<Project> projects(RocketActionSettings settings) throws TodoistRepositoryException {
        List<Project> projects = new ArrayList<>();
        String token = settings.settings().get(TodoistRocketAction.TOKEN);
        if (token == null || "".equals(token)) {
            token = System.getProperty(TOKEN_PROPERTY, "");
        }

        String debugToken = "";
        if (token.length() > 5) {
            debugToken = token.substring(0, 4);
        }
        LOGGER.log(Level.INFO, "method=projects todoistToken={0}", debugToken);

        try {
            Request request = new Request.Builder()
                    .url(BASE_URL)
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
                    final Project[] projectsFromJson = gson.fromJson(text, Project[].class);
                    if (projectsFromJson.length > 0) {
                        projects.addAll(Arrays.asList(projectsFromJson));
                    }
                } else {
                    throw new TodoistRepositoryException(
                            "Exception when get todoist projects with code=" + code + " and text=" + text);
                }
            }

            return projects;
        } catch (Exception e) {
            throw new TodoistRepositoryException("Exception when get todoist projects", e);
        }
    }
}
