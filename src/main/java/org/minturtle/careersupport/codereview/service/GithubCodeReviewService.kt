package org.minturtle.careersupport.codereview.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import lombok.extern.slf4j.Slf4j
import org.kohsuke.github.GHCommit
import org.minturtle.careersupport.codereview.dto.CodeReviewFileInfo
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.entity.CommitPinpoint
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

@Service
@Slf4j
class GithubCodeReviewService(
    private val codeReviewClient: AiCodeReviewClient,
    @Autowired private val reviewPinpointRepository: ReviewPinpointRepository,
    @Value("\${code-review.whitelist.extensions}") private val codeReviewWhiteList: List<String?>
) {

    suspend fun doCodeReview(codeReviewRequest: CodeReviewRequest): List<CodeReviewResponse> {
        val token = codeReviewRequest.githubToken
        val repositoryName = codeReviewRequest.repositoryName
        val prNumber = codeReviewRequest.prNumber

        val pullRequest = GithubPullRequestFacade.of(token, repositoryName, prNumber)

        val reviews = getReviewResponseFromAi(prNumber, repositoryName, pullRequest)

        return reviews
    }

    private suspend fun getReviewResponseFromAi(
        prNumber: Int,
        repositoryName: String,
        pullRequest: GithubPullRequestFacade
    ): List<CodeReviewResponse> = coroutineScope{
        val commitPinpoint = reviewPinpointRepository.findByPrNumberAndRepositoryName(prNumber, repositoryName)
            .awaitSingleOrNull()

        // commitPinpoint가 없으면 모든 changedFile을, 있다면 마지막 커밋 이후의 changedFile을 가져옴
        val changedFiles = commitPinpoint?.let {
            pullRequest.getCommitsDiffAfter(commitPinpoint.lastSha)
        } ?: pullRequest.getCommitsDiff()

        // commitPinPoint를 업데이트
        reviewPinpointRepository.persist(prNumber, changedFiles, commitPinpoint)

        changedFiles.filter { filterWhiteList(it) }
            .map { CodeReviewFileInfo.from(it) }
            .map {
                async{ codeReviewClient.getAiCodeReview(it) }
            }
            .awaitAll()
            .toList()

    }

    private fun ReviewPinpointRepository.persist(
        prNumber: Int,
        files: List<GHCommit.File>,
        commitPinpoint: CommitPinpoint?
    ) {
        commitPinpoint?.let {
            updatePinPoint(commitPinpoint, files)
        } ?: savePinPoint(prNumber, files)
    }

    /**
     * 기존의 CommitPinpoint를 업데이트합니다.
     *
     * @param existingPinpoint 기존의 CommitPinpoint
     * @param files            새로운 파일 리스트
     */
    private fun updatePinPoint(existingPinpoint: CommitPinpoint, files: List<GHCommit.File>){
        val newSha = files[files.size - 1].sha
        existingPinpoint.upDateSha(newSha)
        reviewPinpointRepository.save(existingPinpoint).subscribe()
    }

    /**
     * 새로운 CommitPinpoint를 마지막 커밋 sha와 함께 저장합니다.
     *
     * @param prNumber PR 번호
     * @param files    파일 리스트
     */
    private fun savePinPoint(prNumber: Int, files: List<GHCommit.File>) {
        val lastCommitSha = files[files.size - 1].sha
        val commitPinpoint = CommitPinpoint.builder()
            .lastSha(lastCommitSha)
            .prNumber(prNumber)
            .build()
        reviewPinpointRepository.save(commitPinpoint).subscribe()
    }

    private fun filterWhiteList(file: GHCommit.File): Boolean {
        return codeReviewWhiteList.contains(StringUtils.getFilenameExtension(file.fileName))
    }

}
