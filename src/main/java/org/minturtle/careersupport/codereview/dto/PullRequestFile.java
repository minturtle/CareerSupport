package org.minturtle.careersupport.codereview.dto;


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
}

