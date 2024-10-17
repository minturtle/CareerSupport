package org.minturtle.careersupport.codereview.dto;


import lombok.*;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class CodeReviewRequest {
    private String githubToken;
    private String repositoryName;
    private Integer prNumber;
}