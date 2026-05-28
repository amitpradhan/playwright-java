package day8_api_start;

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
import utils.reports.LogUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ApiHealthCheckTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing Playwright Engine context for OAuth-protected Worker API testing.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);
    }

    @Test(priority = 1, description = "TC13 - Verify billpay-api health using OAuth Client Credentials")
    public void testServiceHealthEndpoint() {
        // Step 1: Format and encode the OAuth Client Credentials (practice_client:practice_secret)
        String clientId = "practice_client";
        String clientSecret = "practice_secret";
        String authString = clientId + ":" + clientSecret;

        // Convert to a secure Base64 Basic Authorization token
        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        // Step 2: Set up the headers map
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + base64Auth);
        headers.put("Accept", "application/json");

        LogUtils.debug("Injecting structural OAuth Basic header sequence into request context.");

        // Step 3: Execute the HTTP GET check using your generic ApiUtils class
        APIResponse response = ApiUtils.get(requestContext, "/health", headers);

        LogUtils.info("Received Health Endpoint Status Code: " + response.status());

        // Step 4: Validate the response status code is exactly 200 OK
        Assert.assertEquals(response.status(), 200, "OAuth authorization rejected or service is down!");

        // Step 5: Extract and parse the JSON string response body via Gson
        String responseBody = response.text();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        // Step 6: Assert the expected response body elements explicitly
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean(), "The response key 'success' was expected to be true!");

        JsonObject dataObject = jsonResponse.getAsJsonObject("data");
        Assert.assertEquals(dataObject.get("status").getAsString(), "healthy");
        Assert.assertEquals(dataObject.get("service").getAsString(), "billpay-api");
        Assert.assertEquals(dataObject.get("version").getAsString(), "v1");

        JsonObject metaObject = jsonResponse.getAsJsonObject("meta");
        Assert.assertTrue(metaObject.has("requestId"));
        Assert.assertEquals(metaObject.get("version").getAsString(), "v1");

        LogUtils.info("Success! OAuth-gated health endpoint metrics validated cleanly.");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Disposing active microservice network API requests context.");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}