package org.minturtle.careersupport.codereview.service;

import lombok.Getter;
import org.kohsuke.github.GHCommit.File;
import reactor.core.publisher.Mono;

public interface AiCodeReviewClient {

    Mono<ReviewResponse> getAiCodeReview(ReviewRequest file);

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

        public static ReviewRequest from(File file) {
            return new ReviewRequest(ReviewFileStatus.valueOf(file.getStatus()),
                    file.getFileName(),
                    file.getPatch());
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
