package org.minturtle.careersupport.common.controller;

import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.testutils.IntegrationTest;

import static org.assertj.core.api.Assertions.*;




class CommonControllerTest extends IntegrationTest {
    @Test
    public void testHealthCheck() {
        webTestClient.get().uri("/api/health-check")
                .exchange()
                .expectStatus().isOk();
    }
}