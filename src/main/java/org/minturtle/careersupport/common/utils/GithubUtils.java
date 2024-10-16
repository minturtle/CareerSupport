package org.minturtle.careersupport.common.utils;

import org.minturtle.careersupport.common.facade.GithubPullRequestFacade;
import org.springframework.stereotype.Component;


@Component
public class GithubUtils {

    public GithubPullRequestFacade generatePullRequest(String token, String repositoryName, int prNumber){
        return new GithubPullRequestFacade(token, repositoryName, prNumber);
    }

}