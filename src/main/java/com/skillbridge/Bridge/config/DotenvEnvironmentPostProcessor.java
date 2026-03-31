package com.skillbridge.Bridge.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@code .env} from the process working directory (e.g. bridge-backend/) so
 * {@code MYSQL_*} and {@code ALLOWED_ORIGINS} apply when running from the IDE without {@code source .env}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "bridgeDotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(SOURCE_NAME)) {
            return;
        }
        Path dotenv = Path.of(".env");
        if (!Files.isRegularFile(dotenv)) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(dotenv)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        } catch (IOException ignored) {
            return;
        }
        if (!map.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, map));
        }
    }
}
