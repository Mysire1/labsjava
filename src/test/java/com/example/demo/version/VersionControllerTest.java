package com.example.demo.version;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VersionControllerTest {

    @Test
    void returnsConfiguredVersionAndPod() {
        assertThat(new VersionController("1.2.3", "pod-1").version())
                .isEqualTo(Map.of("version", "1.2.3", "pod", "pod-1"));
    }
}
