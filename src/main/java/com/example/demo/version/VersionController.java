package com.example.demo.version;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {

    private final String version;
    private final String pod;

    public VersionController(@Value("${app.version}") String version, @Value("${HOSTNAME:local}") String pod) {
        this.version = version;
        this.pod = pod;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("version", version, "pod", pod);
    }
}
