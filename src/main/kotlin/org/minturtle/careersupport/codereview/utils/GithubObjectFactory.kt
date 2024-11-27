package org.minturtle.careersupport.codereview.utils

import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.springframework.stereotype.Component

@Component
class GithubObjectFactory {


    suspend fun createFacade(
        githubToken: String,
        repositoryName: String,
        prNumber: Int
    ): GithubPullRequestFacade {
        return GithubPullRequestFacade.of(githubToken, repositoryName, prNumber)

    }
}