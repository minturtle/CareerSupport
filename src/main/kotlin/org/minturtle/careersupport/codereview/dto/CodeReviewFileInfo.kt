package org.minturtle.careersupport.codereview.dto

import org.kohsuke.github.GHCommit
import org.minturtle.careersupport.codereview.enums.ReviewFileStatus


data class CodeReviewFileInfo(
    val status: ReviewFileStatus,
    val fileName: String,
    val content: String,
){

    companion object{
        fun from(file: GHCommit.File): CodeReviewFileInfo {
            return CodeReviewFileInfo(
                ReviewFileStatus.valueOf(file.status),
                file.fileName,
                file.patch
            )
        }

    }


}