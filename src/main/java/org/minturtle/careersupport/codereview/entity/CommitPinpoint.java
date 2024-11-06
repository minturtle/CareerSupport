package org.minturtle.careersupport.codereview.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Document(collection = "commit_pinpoint")
public class CommitPinpoint {
    @Id
    private String id;

    private String lastSha;

    private Integer prNumber;

    private String repositoryName;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void upDateSha(String newSha) {
        this.lastSha = newSha;
    }
}
