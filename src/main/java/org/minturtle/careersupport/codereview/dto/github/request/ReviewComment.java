package org.minturtle.careersupport.codereview.dto.github.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @see <a
 * href="https://docs.github.com/ko/rest/pulls/comments?apiVersion=2022-11-28#create-a-review-comment-for-a-pull-request">리뷰
 * 작성 요청</a>
 */
@Getter
@Builder
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ReviewComment {
    private String body;

    private String commitId;

    private String path;

    private Integer position;

    private Integer line;

    private Side side;

    @JsonProperty("start_line")
    private Integer startLine;

    @JsonProperty("start_side")
    private Side startSide;

    @JsonProperty("in_reply_to")
    private Integer inReplyTo;

    @JsonProperty("subject_type")
    private String subjectType;

    public enum Side {
        LEFT, RIGHT
    }

    public enum SubjectType {
        PULL_REQUEST_REVIEW_COMMENT("pull_request_review_comment");

        private final String value;

        SubjectType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
