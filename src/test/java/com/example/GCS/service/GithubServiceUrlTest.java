package com.example.GCS.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubServiceUrlTest {

    // push日時の新しい順で1件だけ取得するURLになっていること
    // (デフォルトの名前順・30件だと、31件目以降のリポジトリへのpushを見落とすため)
    @Test
    void buildLatestPushedRepoUrl_sortsByPushedAndLimitsToOne() {
        String url = GithubService.buildLatestPushedRepoUrl("octocat");
        assertEquals("https://api.github.com/users/octocat/repos?sort=pushed&per_page=1", url);
    }
}
