package org.minturtle.careersupport.codereview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CodeReviewRequest {
    private String githubToken;
    private String repositoryName;
    private Long prNumber;
}
