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

    public static synchronized String getValidToken(APIRequestContext requestContext) {
        long currentSystemTime = System.currentTimeMillis();

        if (cachedAccessToken != null && currentSystemTime < (tokenExpiryEpochMillis - 5000)) {
            return cachedAccessToken;
        }

        LogUtils.info("OAuth2 token missing or expired. Initiating fresh POST /oauth/token handshake...");

        // Formulate a clean, raw query string just like Postman's x-www-form-urlencoded tab does
        String urlEncodedBody = "grant_type=client_credentials" +
                "&client_id=demo" +
                "&client_secret=password123";

        // Pass it via .setData() with the correct Content-Type header explicitly stated
        APIResponse response = requestContext.post("/oauth/token",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/x-www-form-urlencoded")
                        .setHeader("Accept", "application/json")
                        .setData(urlEncodedBody)
        );

        if (response.status() != 200) {
            String errorMsg = "OAuth2 Handshake Failed! HTTP Status: " + response.status() + " Body: " + response.text();
            LogUtils.info("[CRITICAL AUTH ERROR] " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        cachedAccessToken = jsonResponse.get("access_token").getAsString();

        long expiresInSeconds = jsonResponse.has("expires_in") ? jsonResponse.get("expires_in").getAsLong() : 3600;
        tokenExpiryEpochMillis = System.currentTimeMillis() + (expiresInSeconds * 1000);

        LogUtils.info("Fresh OAuth2 Access Token obtained successfully. Expires in: " + expiresInSeconds + "s");

        return cachedAccessToken;
    }

    public static void forceTokenExpiration() {
        cachedAccessToken = null;
        tokenExpiryEpochMillis = 0;
    }
}