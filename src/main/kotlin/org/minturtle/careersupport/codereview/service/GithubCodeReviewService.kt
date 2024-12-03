package org.minturtle.careersupport.codereview.service

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.kohsuke.github.GHCommit
import org.minturtle.careersupport.codereview.dto.CodeReviewFileInfo
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.entity.CommitPinpoint
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository
import org.minturtle.careersupport.codereview.utils.GithubObjectFactory
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
class GithubCodeReviewService(
    private val codeReviewClient: AiCodeReviewClient,
    @Autowired private val reviewPinpointRepository: ReviewPinpointRepository,
    @Autowired private val githubObjectFactory: GithubObjectFactory,
    @Value("\${code-review.whitelist.extensions}") private val codeReviewWhiteList: List<String>
) {

    suspend fun doCodeReview(codeReviewRequest: CodeReviewRequest): List<CodeReviewResponse> {
        val token = codeReviewRequest.githubToken
        val repositoryName = codeReviewRequest.repositoryName
        val prNumber = codeReviewRequest.prNumber

        val pullRequest = githubObjectFactory.createFacade(
            githubToken = token,
            repositoryName = repositoryName,
            prNumber = prNumber
        )

        val reviews = getReviewResponseFromAi(prNumber, repositoryName, pullRequest)

        return reviews
    }

    private suspend fun getReviewResponseFromAi(
        prNumber: Int,
        repositoryName: String,
        pullRequest: GithubPullRequestFacade
    ): List<CodeReviewResponse> = coroutineScope{
        val commitPinpoint = reviewPinpointRepository.findByPrNumberAndRepositoryName(prNumber, repositoryName)

        // commitPinpoint가 없으면 모든 changedFile을, 있다면 마지막 커밋 이후의 changedFile을 가져옴
        val changedFiles = commitPinpoint?.let {
            pullRequest.getCommitsDiffAfter(commitPinpoint.lastSha)
        } ?: pullRequest.getCommitsDiff()

        // commitPinPoint를 업데이트
        reviewPinpointRepository.persist(prNumber, repositoryName, changedFiles, commitPinpoint)

        changedFiles.filter { filterWhiteList(it) }
            .map { CodeReviewFileInfo.from(it) }
            .map {
                // 비동기로 동시에 실행
                async{ codeReviewClient.getAiCodeReview(it) }
            }
            .awaitAll()
            .toList()

    }

    private suspend fun ReviewPinpointRepository.persist(
        prNumber: Int,
        repositoryName: String,
        files: List<GHCommit.File>,
        commitPinpoint: CommitPinpoint?
    ) {
        commitPinpoint?.let {
            updatePinPoint(commitPinpoint, files)
        } ?: savePinPoint(prNumber, repositoryName, files)
    }

    /**
     * 기존의 CommitPinpoint를 업데이트합니다.
     *
     * @param existingPinpoint 기존의 CommitPinpoint
     * @param files            새로운 파일 리스트
     */
    private suspend fun updatePinPoint(existingPinpoint: CommitPinpoint, files: List<GHCommit.File>){
        val newSha = files[files.size - 1].sha
        existingPinpoint.lastSha = newSha
        reviewPinpointRepository.save(existingPinpoint)
    }

    /**
     * 새로운 CommitPinpoint를 마지막 커밋 sha와 함께 저장합니다.
     *
     * @param prNumber PR 번호
     * @param files    파일 리스트
     */
    private suspend fun savePinPoint(prNumber: Int, repositoryName: String, files: List<GHCommit.File>) {
        val lastCommitSha = files[files.size - 1].sha
        val commitPinpoint = CommitPinpoint(
            lastSha = lastCommitSha,
            prNumber = prNumber,
            repositoryName = repositoryName
        )
        reviewPinpointRepository.save(commitPinpoint)
    }

    private fun filterWhiteList(file: GHCommit.File): Boolean {
        return codeReviewWhiteList.contains(StringUtils.getFilenameExtension(file.fileName))
    }

}