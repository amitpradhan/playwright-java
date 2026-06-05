package day10;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ApiUtils;
import utils.api.AuthTokenManager;
import utils.reports.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class ApiOAuth2Test {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private String activeToken;

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing Playwright Context for OAuth2 Authentication Pipeline verification.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);
    }

    @Test(priority = 1, description = "OAuth2 - Validate token retrieval and extraction processing rules")
    public void testExtractAccessToken() {
        // Fetch the token using the newly implemented utility manager
        activeToken = AuthTokenManager.getValidToken(requestContext);

        Assert.assertNotNull(activeToken, "The OAuth2 manager failed to return a token!");
        Assert.assertFalse(activeToken.trim().isEmpty(), "Returned Access Token string token length is empty!");
        LogUtils.info("Step 1 Pass: Access token successfully verified and saved into execution thread state memory.");
    }

    @Test(priority = 2, dependsOnMethods = {"testExtractAccessToken"}, description = "OAuth2 - Call /v1/auth/me to verify token works")
    public void testVerifyTokenWithAuthMeEndpoint() {
        LogUtils.info("Executing: GET /v1/auth/me using Bearer authorization wrapper framework.");

        // Construct standard Authorization: Bearer <token> headers map
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + activeToken);
        headers.put("Accept", "application/json");

        APIResponse response = ApiUtils.get(requestContext, "/v1/auth/me", headers);
        LogUtils.info("GET /v1/auth/me Status Code: " + response.status());

        // Assert token was accepted successfully
        Assert.assertEquals(response.status(), 200, "The secured data resource endpoint rejected the access token!");

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean(), "Expected base payload container flag 'success' to be true.");

        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        Assert.assertTrue(dataObj.has("client"), "Missing identity description object parameters inside data profile model.");
        Assert.assertEquals(dataObj.get("client").getAsString(), "practice_client", "Identity context configuration mapping error!");
    }

    @Test(priority = 3, dependsOnMethods = {"testVerifyTokenWithAuthMeEndpoint"}, description = "OAuth2 - Verify token expiration handling mechanisms")
    public void testTokenExpirationAndAutoRefresh() {
        LogUtils.info("Validating Token Expiration Handling...");

        // Capture initial token pointer state references
        String initialToken = activeToken;

        // Force a simulated expiration inside our token state engine cache layers
        AuthTokenManager.forceTokenExpiration();

        // Request token again—the utility must detect expiration and transparently fetch a fresh token
        String autoRefreshedToken = AuthTokenManager.getValidToken(requestContext);

        Assert.assertNotNull(autoRefreshedToken, "The auto-refresh mechanism returned a null token state pointer!");

        // Send a fresh payload query to confirm the new token operates securely
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + autoRefreshedToken);
        headers.put("Accept", "application/json");

        APIResponse response = ApiUtils.get(requestContext, "/v1/auth/me", headers);
        Assert.assertEquals(response.status(), 200, "The auto-refreshed token was rejected by the gateway server application layer!");

        LogUtils.info("Success! Automated OAuth2 Token Lifecycle pipeline functions flawlessly.");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Tearing down OAuth2 test automation environments.");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}