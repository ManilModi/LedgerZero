package org.example.service.imp;

import org.example.model.AccessToken;
import org.example.repository.AccessTokenRepo;
import org.example.service.IAccessToken;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenManagerService implements IAccessToken {

    @Autowired
    private AccessTokenRepo tokenRepository;

    private final String SERVICE_NAME = "SANDBOX_CO";

    /**
     * 1.  Checks expiry before returning.
     */
    public synchronized String getAccessToken() {
        // Fetch from DB
        AccessToken token = tokenRepository.findByToken(SERVICE_NAME);

        // CHECK: Is it missing? OR Is it expired (or expiring in next 5 mins)?
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            System.out.println("⚠️ Token expired or missing during API call. Refreshing now...");
            return refreshSandboxToken(); // Force refresh immediately
        }

        return token.getToken();
    }

    /**
     * 2. Fetches new token from Sandbox API and saves to DB.
     */
    private String refreshSandboxToken() {
        try {
            System.out.println("🔄 Fetching new Access Token from Sandbox...");
             RestTemplate restTemplate = new RestTemplate();
             Map<String, String> body = new HashMap<>();
             body.put("api_key", "YOUR_KEY");
             body.put("api_secret", "YOUR_SECRET");
             Map response = restTemplate.postForObject("https://api.sandbox.co.in/authenticate", body, Map.class);

            String newToken = ((Map<?, ?>) response.get("data")).get("access_token").toString();

            // Save to DB with 24h expiry
            AccessToken token = new AccessToken();
            token.setServiceName(SERVICE_NAME);
            token.setToken(newToken);
            token.setExpiresAt(LocalDateTime.now().plusHours(24));
            token.setUpdatedAt(LocalDateTime.now());
            tokenRepository.save(token);

            System.out.println("✅ Token Refreshed & Saved to DB!");
            return newToken;

        } catch (Exception e) {
            System.err.println("❌ Failed to refresh token: " + e.getMessage());
            throw new RuntimeException("Critical: Could not refresh API Token");
        }
    }

    /**
     * 3. Still runs every 23h to update in background so users don't wait.
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 23)
    public void scheduledRefresh() {
        refreshSandboxToken();
    }

    /**
     * 4. Checks immediately if we have a valid token on boot.
     */
    @PostConstruct
    public void init() {
        AccessToken token = tokenRepository.findByToken(SERVICE_NAME);
        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.out.println("🚀 Startup: Token invalid. Fetching fresh one...");
            refreshSandboxToken();
        }
    }
}
