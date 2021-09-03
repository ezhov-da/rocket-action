package ru.ezhov.rocket.action.types.gist

import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GistService
import ru.ezhov.rocket.action.api.RocketActionSettings
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class GistActionService {
    @Throws(GistActionServiceException::class)
    fun gists(settings: RocketActionSettings): List<Gist> {
        val gitHubClient = GitHubClient()
        var token = settings.settings()[GistRocketAction.TOKEN]
        if ("" == token) {
            token = System.getProperty(TOKEN_PROPERTY, "")
        }
        return token?.takeIf { it.isNotEmpty() }?.let { tok ->
            var debugToken = ""
            if (tok.length > 5) {
                debugToken = tok.substring(0, 4)
            }
            LOGGER.log(Level.INFO, "method=gists gistToken={0}", debugToken)
            gitHubClient.setOAuth2Token(tok)
            val gistService = GistService(gitHubClient)
            val username = settings.settings()["username"]
            try {
                gistService.getGists(username)
            } catch (e: IOException) {
                throw GistActionServiceException("Error catch list Gist for user '$username'", e)
            }
        } ?: emptyList()
    }

    companion object {
        const val TOKEN_PROPERTY = "rocket.action.gist.token"
        private val LOGGER = Logger.getLogger(GistActionService::class.java.name)
    }
}