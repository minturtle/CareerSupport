package org.minturtle.careersupport.common.facade;

import java.util.Arrays;
import org.kohsuke.github.*;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCompare.Commit;

import java.io.IOException;
import java.util.List;

import org.minturtle.careersupport.common.exception.InternalServerException;
import reactor.core.publisher.Flux;

public class GithubPullRequestFacade {

    public final GitHub gitHub;
    private final GHRepository githubRepository;
    private final GHPullRequest pullRequest;

    public GithubPullRequestFacade(String gitHubToken, String repositoryName, int prNumber) {
        try {
            this.gitHub = new GitHubBuilder().withOAuthToken(gitHubToken).build();
            this.githubRepository = gitHub.getRepository(repositoryName);
            this.pullRequest = githubRepository.getPullRequest(prNumber);
        } catch (IOException e) {
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    public List<GHPullRequestFileDetail> getChangedFiles() {
        try {
            return pullRequest.listFiles().toList();
        } catch (IOException e) {
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * @param lastCommitSha 가장 마지막 커밋 sha
     * @return 추가 push 이후 변경사항
     */
    public Flux<File> getCommitsDiffAfter(String lastCommitSha) {
        String headSha = pullRequest.getHead().getSha();
        try {
            GHCompare compare = githubRepository.getCompare(lastCommitSha, headSha);

            return Flux.fromIterable(Arrays.stream(compare.getCommits()).toList())
                    .filter(commit -> isCommitAfter(lastCommitSha, commit))
                    .flatMap(this::getFileStream);
        } catch (IOException e) {
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * @return 최초 pr시 변경사항
     */
    public Flux<File> getCommitsDiff() {
        String baseSha = pullRequest.getBase().getSha();
        String headSha = pullRequest.getHead().getSha();
        try {
            GHCompare compare = githubRepository.getCompare(baseSha, headSha);
            return Flux.fromArray(compare.getFiles());
        } catch (IOException e) {
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    private boolean isCommitAfter(String lastCommitSha, GHCommit commit) {
        return commit.getParentSHA1s().contains(lastCommitSha)// 이전 커밋들이 가장 마지막 SAH를 가지고 있음 -> 최신 커밋은 가능함
                && !commit.getSHA1().equals(lastCommitSha);//그리고 자기가 그 마지막 커밋이 아님
    }

    private Flux<File> getFileStream(Commit commit) {
        try {
            return Flux.fromIterable(commit.listFiles());
        } catch (IOException e) {
            throw new InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }
}
