package org.minturtle.careersupport.codereview.dto.github.response;

import lombok.Builder;
import org.kohsuke.github.GHPullRequestFileDetail;

/**
 * @see <a href="https://docs.github.com/en/rest/pulls/pulls?apiVersion=2022-11-28#list-pull-requests-files">변경 파일 조회
 * 요청</a>
 */

@Builder
public record PullRequestFile(String sha,
                              String filename,
                              String status, // "added","removed","modified","renamed","copied","changed","unchanged"
                              Integer additions,//추가 줄 수
                              Integer deletions,//삭제 줄 수
                              Integer changes,//총 변경사항 수
                              String blobUrl,
                              String rawUrl,
                              String contentsUrl,
                              String patch) {
    public static PullRequestFile from(GHPullRequestFileDetail file) {
        return new PullRequestFile(
                file.getSha(),
                file.getFilename(),
                file.getStatus(),
                file.getAdditions(),
                file.getDeletions(),
                file.getChanges(),
                file.getBlobUrl().getPath(),
                file.getRawUrl().getPath(),
                file.getContentsUrl().getPath(),
                file.getPatch()
        );
    }
}
