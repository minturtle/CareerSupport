package org.minturtle.careersupport.codereview.service;

import lombok.Getter;
import org.kohsuke.github.GHPullRequestFileDetail;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiCodeReviewClient {

    Flux<ReviewResponse> getAiCodeReview(List<ReviewRequest> files);

    @Getter
    final class ReviewRequest {
        private final ReviewFileStatus status;
        private final String fileName;
        private final String content;


        public ReviewRequest(ReviewFileStatus status, String fileName, String content) {
            this.status = status;
            this.fileName = fileName;
            this.content = content;
        }

        public static ReviewRequest from(GHPullRequestFileDetail ghPullRequestFileDetail) {
            return new ReviewRequest(ReviewFileStatus.valueOf(ghPullRequestFileDetail.getStatus()),
                    ghPullRequestFileDetail.getFilename(),
                    ghPullRequestFileDetail.getPatch());
        }
    }

    enum ReviewFileStatus {
        added, removed, modified, renamed, copied, changed, unchanged
    }

    @Getter
    final class ReviewResponse {
        private final String fileName;
        private final String reviewContent;

        public ReviewResponse(String fileName, String reviewContent) {
            this.fileName = fileName;
            this.reviewContent = reviewContent;
        }
    }
}
