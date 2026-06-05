package utils.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import utils.reports.LogUtils;

public class AuthTokenManager {
    private static String cachedAccessToken = null;
    private static long tokenExpiryEpochMillis = 0;

    /**
     * Retrieves a valid access token. Forces raw text stream delivery
     * to prevent Playwright from appending unwanted 'charset=utf-8' modifiers.
     */
    public static synchronized String getValidToken(APIRequestContext requestContext) {
        long currentSystemTime = System.currentTimeMillis();

        if (cachedAccessToken != null && currentSystemTime < (tokenExpiryEpochMillis - 5000)) {
            LogUtils.debug("Utilizing cached OAuth2 Bearer token. Time remaining: "
                    + (tokenExpiryEpochMillis - currentSystemTime) / 1000 + "s");
            return cachedAccessToken;
        }

        LogUtils.info("OAuth2 token missing or expired. Initiating fresh POST /oauth/token handshake...");

        // Step 1: Construct an explicit, raw JSON payload string
        String rawJsonBody = "{"
                + "\"grant_type\":\"client_credentials\","
                + "\"client_id\":\"practice_client\","
                + "\"client_secret\":\"practice_secret\""
                + "}";

        // Step 2: Inject the raw string using .setData()
        APIResponse response = requestContext.post("/oauth/token",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json") // Clean header override
                        .setHeader("Accept", "application/json")
                        .setData(rawJsonBody)
        );

        if (response.status() != 200) {
            String errorMsg = "OAuth2 Handshake Failed! HTTP Status: " + response.status() + " Body: " + response.text();
            LogUtils.info("[CRITICAL AUTH ERROR] " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // Parse token metrics out of the payload response
        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        cachedAccessToken = jsonResponse.get("access_token").getAsString();

        long expiresInSeconds = jsonResponse.has("expires_in") ? jsonResponse.get("expires_in").getAsLong() : 3600;
        tokenExpiryEpochMillis = System.currentTimeMillis() + (expiresInSeconds * 1000);

        LogUtils.info("Fresh OAuth2 Access Token obtained successfully. Expires in: " + expiresInSeconds + "s");

        return cachedAccessToken;
    }

    public static void forceTokenExpiration() {
        LogUtils.warn("Simulating token expiration state: Local cache explicitly wiped.");
        cachedAccessToken = null;
        tokenExpiryEpochMillis = 0;
    }
}