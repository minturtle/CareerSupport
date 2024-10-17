package org.minturtle.careersupport.common.facade;

import org.kohsuke.github.*;
import org.minturtle.careersupport.common.exception.InternalServerException;

import java.io.IOException;
import java.util.List;

public class GithubPullRequestFacade {

    public final GitHub gitHub;
    private final GHRepository githubRepository;
    private final GHPullRequest pullRequest;

    public GithubPullRequestFacade(String gitHubToken, String repositoryName, int prNumber){
        try {
            this.gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();;
            this.githubRepository = gitHub.getRepository(repositoryName);
            this.pullRequest = githubRepository.getPullRequest(prNumber);
        }catch (IOException e){
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    public List<GHPullRequestFileDetail> getChangedFiles(){
        try {
            return pullRequest.listFiles().toList();
        }catch (IOException e){
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

}