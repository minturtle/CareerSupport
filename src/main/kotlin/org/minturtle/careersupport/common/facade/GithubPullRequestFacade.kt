package org.minturtle.careersupport.common.facade

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kohsuke.github.*
import org.minturtle.careersupport.common.exception.InternalServerException
import java.io.IOException
import java.util.*
import java.util.stream.Stream

class GithubPullRequestFacade(
    private val githubRepository: GHRepository,
    private val pullRequest: GHPullRequest
) {


    companion object{
        suspend fun of(gitHubToken: String, repositoryName: String, prNumber: Int): GithubPullRequestFacade {
            return withContext(Dispatchers.IO){
                try {
                    val gitHub = GitHubBuilder().withOAuthToken(gitHubToken).build()
                    val githubRepository = gitHub.getRepository(repositoryName)
                    val pullRequest = githubRepository.getPullRequest(prNumber)

                    GithubPullRequestFacade(
                        githubRepository = githubRepository,
                        pullRequest = pullRequest
                    )
                } catch (e: IOException) {
                    throw InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", e)
                }
            }
        }
    }

    fun getChangedFiles() : List<GHPullRequestFileDetail> {
        return runCatching {
            pullRequest.listFiles().toList()
        }.onFailure {
            throw InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", it)
        }.getOrThrow()
    }

    /**
     * @param lastCommitSha 가장 마지막 커밋 sha
     * @return 추가 push 이후 변경사항
     */
    fun getCommitsDiffAfter(lastCommitSha: String): List<GHCommit.File> {
        val headSha = pullRequest.head.sha
        return runCatching {
            val compare = githubRepository.getCompare(lastCommitSha, headSha)

            Arrays.stream(compare.commits)
                .filter{ commit->
                    isCommitAfter(
                        lastCommitSha,
                        commit
                    )
                }.flatMap {
                    getFileStream(it)
                }.toList().filterNotNull()

        }.onFailure {
            throw InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", it)
        }.getOrThrow()
    }
    /**
     * @return 최초 pr시 변경사항
     */
    fun getCommitsDiff(): List<GHCommit.File>{
            return runCatching {
                val baseSha = pullRequest.base.sha
                val headSha = pullRequest.head.sha

                val compare = githubRepository.getCompare(baseSha, headSha)
                Arrays.stream(compare.getFiles()).toList()
            }.onFailure {
                throw InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", it)
            }.getOrThrow()


        }

    private fun isCommitAfter(lastCommitSha: String, commit: GHCommit): Boolean {
        return (commit.getParentSHA1s().contains(lastCommitSha) // 이전 커밋들이 가장 마지막 SAH를 가지고 있음 -> 최신 커밋은 가능함
                && commit.shA1 != lastCommitSha //그리고 자기가 그 마지막 커밋이 아님
                )
    }

    private fun getFileStream(commit: GHCompare.Commit): Stream<GHCommit.File?> {
        return runCatching {
            commit.listFiles().toList().stream()
        }.onFailure {
            throw InternalServerException("Github 서버와 통신 중 오류가 발생했습니다.", it)
        }.getOrThrow()
    }
}
