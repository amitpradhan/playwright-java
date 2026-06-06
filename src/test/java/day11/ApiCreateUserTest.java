package day11;

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

import java.util.HashMap;
import java.util.Map;

public class ApiCreateUserTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private Map<String, String> headers;

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing Playwright Engine context for User Creation Test.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);

        // Define your required custom X-API-Key authentication mapping
        headers = new HashMap<>();
        headers.put("X-API-Key", "pk_practice_1234567890");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
    }

    @Test(description = "TC15 - Verify successful user creation via POST endpoint")
    public void testCreateNewUser() {
        LogUtils.info("Executing: POST /v1/users");

        // Step 1: Formulate the payload dictionary with dynamic data to prevent duplicate record collisions
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "automation_nishant_" + System.currentTimeMillis() + "@example.com");
        payload.put("phone", "+9198765" + (int)(Math.random() * 90000 + 10000)); // Non-null random 10 digit structure
        payload.put("firstName", "Nishant");
        payload.put("lastName", "Kumar");
        payload.put("kycStatus", "pending");

        // Step 2: Use your generic ApiUtils class wrapper to fire the request
        APIResponse response = ApiUtils.post(requestContext, "/v1/users", payload, headers);
        LogUtils.info("POST Response Status Code: " + response.status());

        // Step 3: Assert server handles the resource allocation correctly (200 OK or 201 Created)
        Assert.assertTrue(response.status() == 201 || response.status() == 200,
                "User creation failed! Returned status code: " + response.status() + " Body: " + response.text());

        // Step 4: Validate tracking properties inside response string json tree structure
        String responseBody = response.text();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        // Assert container envelope rules match success criteria
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean(), "Expected base execution container flag 'success' to yield true.");

        // Step 5: Verify the created record returned elements
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        Assert.assertTrue(dataObj.has("id"), "The response payload data body is missing the assigned user identity tracker: 'id'");

        String generatedId = dataObj.get("id").getAsString();
        LogUtils.info("Success! New record committed to tracking index database under ID: " + generatedId);

        Assert.assertEquals(dataObj.get("firstName").getAsString(), "Nishant", "Persisted field mapping 'firstName' mismatch!");
        Assert.assertEquals(dataObj.get("lastName").getAsString(), "Kumar", "Persisted field mapping 'lastName' mismatch!");
        Assert.assertEquals(dataObj.get("kycStatus").getAsString(), "pending", "Persisted status tracker initial state mismatch!");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Disposing active user creation network context environments.");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}