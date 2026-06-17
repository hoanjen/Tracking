package com.tracking.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    public Map<String, String> verifyToken(String token) throws Exception {
        try {
            String response = restTemplate.getForObject(
                    GOOGLE_TOKEN_INFO_URL + URLEncoder.encode(token, StandardCharsets.UTF_8),
                    String.class
            );
            JsonElement jsonElement = JsonParser.parseString(response);

            Map<String, String> result = new HashMap<>();
            if (jsonElement.getAsJsonObject().has("email")) {
                result.put("email", jsonElement.getAsJsonObject().get("email").getAsString());
                result.put("name", jsonElement.getAsJsonObject().has("name")
                        ? jsonElement.getAsJsonObject().get("name").getAsString()
                        : jsonElement.getAsJsonObject().get("email").getAsString().split("@")[0]);
                result.put("sub", jsonElement.getAsJsonObject().get("sub").getAsString());
                return result;
            }
            throw new Exception("Invalid token");
        } catch (Exception e) {
            throw new Exception("Failed to verify Google token: " + e.getMessage());
        }
    }
}
