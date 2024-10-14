package org.minturtle.careersupport.codereview.service;

import lombok.Getter;
import org.minturtle.careersupport.codereview.dto.PullRequestFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiCodeReviewClient {

    Flux<ReviewResponse> getAiCodeReview(List<PullRequestFile> files);

    @Getter
    final class ReviewRequest{
        private final ReviewFileStatus status;
        private final String fileName;
        private final String content;


        public ReviewRequest(ReviewFileStatus status, String fileName, String content) {
            this.status = status;
            this.fileName = fileName;
            this.content = content;
        }

        public static ReviewRequest of(PullRequestFile prFile){
            return new ReviewRequest(ReviewFileStatus.valueOf(prFile.status()), prFile.filename(), prFile.patch());
        }
    }

    enum ReviewFileStatus{
        added, removed, modified, renamed, copied, changed, unchanged
    }

    @Getter
    final class ReviewResponse{
        private final String fileName;
        private final String reviewContent;

        public ReviewResponse(String fileName, String reviewContent) {
            this.fileName = fileName;
            this.reviewContent = reviewContent;
        }
    }
}
