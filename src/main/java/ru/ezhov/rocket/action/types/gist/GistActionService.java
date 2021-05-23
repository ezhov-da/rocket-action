package ru.ezhov.rocket.action.types.gist;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.io.IOException;
import java.util.List;

public class GistActionService {
    public static final String TOKEN_PROPERTY = "rocket.action.gist.token";

    public List<Gist> gists(RocketActionSettings settings) throws GistActionServiceException {
        GitHubClient gitHubClient = new GitHubClient();
        String token = settings.settings().get(GistRocketAction.TOKEN);
        if (token == null) {
            token = System.getProperty(TOKEN_PROPERTY);
        }
        gitHubClient.setOAuth2Token(token);
        GistService gistService = new GistService(gitHubClient);
        String username = settings.settings().get("username");
        try {
            return gistService.getGists(username);
        } catch (IOException e) {
            throw new GistActionServiceException("Error catch list Gist for user '" + username + "'", e);
        }
    }
}
