package ru.ezhov.rocket.action.types.gist;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GistActionService {
    public static final String TOKEN_PROPERTY = "rocket.action.gist.token";
    private static final Logger LOGGER = Logger.getLogger(GistActionService.class.getName());

    public List<Gist> gists(RocketActionSettings settings) throws GistActionServiceException {
        GitHubClient gitHubClient = new GitHubClient();
        String token = settings.settings().get(GistRocketAction.TOKEN);
        if (token == null || "".equals(token)) {
            token = System.getProperty(TOKEN_PROPERTY, "");
        }

        String debugToken = "";
        if (token.length() > 5) {
            debugToken = token.substring(0, 4);
        }
        LOGGER.log(Level.INFO, "method=gists gistToken={0}", debugToken);

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
