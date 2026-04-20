package com.java.config;

import java.io.File;
import java.net.URL;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedTomcatWebappConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> tomcatDocumentRootCustomizer() {
        return factory -> {
            File webappDir = resolveWebappDir();
            if (webappDir != null) {
                factory.setDocumentRoot(webappDir);
            }
        };
    }

    private File resolveWebappDir() {
        // 1. 상대 경로로 먼저 시도 (IDE 실행 시 작업 디렉토리 기준)
        String[] relativeCandidates = {
                "src/main/webapp",
                "projectx/src/main/webapp"
        };

        for (String candidate : relativeCandidates) {
            File dir = new File(candidate).getAbsoluteFile();
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }

        // 2. 클래스 파일 위치에서 프로젝트 루트를 역추적
        try {
            URL location = EmbeddedTomcatWebappConfig.class
                    .getProtectionDomain().getCodeSource().getLocation();
            File classesDir = new File(location.toURI()).getAbsoluteFile();

            // build/classes/java/main → build/classes/java → build/classes → build → project root
            File dir = classesDir;
            for (int i = 0; i < 5; i++) {
                File candidate = new File(dir, "src/main/webapp");
                if (candidate.exists() && candidate.isDirectory()) {
                    return candidate;
                }
                dir = dir.getParentFile();
                if (dir == null) break;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}