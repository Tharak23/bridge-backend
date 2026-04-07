package com.skillbridge.Bridge;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BridgeApplication {

	private static final String DEFAULT_CLERK_JWKS =
			"https://concrete-kit-81.clerk.accounts.dev/.well-known/jwks.json";

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();
		// Prefer .env only (not OS env): a stale CLERK_JWKS_URI or SPRING_SECURITY_* in the shell/IDE
		// would otherwise override application.properties and keep hitting the wrong Clerk JWKS.
		String jwksUri = dotenv.get("CLERK_JWKS_URI");
		if (jwksUri == null || jwksUri.isBlank()) {
			jwksUri = DEFAULT_CLERK_JWKS;
		}
		System.setProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", jwksUri.trim());

		Map<String, Object> fromFile = new HashMap<>();
		dotenv.entries().forEach(e -> {
			String key = e.getKey();
			if (System.getenv(key) == null && System.getProperty(key) == null) {
				fromFile.put(key, e.getValue());
			}
		});
		SpringApplication app = new SpringApplication(BridgeApplication.class);
		app.setDefaultProperties(fromFile);
		app.run(args);
	}

}
