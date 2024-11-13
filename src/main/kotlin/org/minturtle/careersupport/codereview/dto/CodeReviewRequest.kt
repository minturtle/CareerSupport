package org.minturtle.careersupport.codereview.dto

data class CodeReviewRequest(
    val githubToken: String,
    val repositoryName: String,
    val prNumber: Int
)