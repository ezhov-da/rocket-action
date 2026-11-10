package ru.ezhov.rocket.action.plugin.gist

import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GistService
import java.io.IOException

class GistActionService {
    @Throws(GistActionServiceException::class)
    fun gists(token: String, username: String): List<Gist> {
        val gitHubClient = GitHubClient()
        gitHubClient.setOAuth2Token(token)
        val gistService = GistService(gitHubClient)

        return try {
            gistService.getGists(username)
        } catch (e: IOException) {
            throw GistActionServiceException("Error catch list Gist for user '$username'", e)
        }
    }
}