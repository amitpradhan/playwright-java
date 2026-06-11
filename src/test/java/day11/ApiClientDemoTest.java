package day11;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.api.ApiClient;
import utils.api.ApiClient.ApiResponse;
import utils.reports.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example test class demonstrating the usage of ApiClient utility
 * Shows various HTTP methods (POST, GET, PUT, DELETE) with fluent API design
 */
public class ApiClientDemoTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private String createdUserId;
    private String dynamicEmail;

    @BeforeClass
    public void setup() {
        LogUtils.info("Setting up test environment with ApiClient...");
        playwright = Playwright.create();
        
        // Initialize request context with base URL
        requestContext = playwright.request().newContext(
                new com.microsoft.playwright.APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
        );
        
        // Generate dynamic data
        dynamicEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @Test(priority = 1, description = "Demo - POST request with fluent API and assertions")
    public void testPostRequestWithFluentApi() {
        LogUtils.info("=== Demo 1: POST Request with Fluent API ===");

        // Build headers with API key
        Map<String, String> headers = ApiClient.buildHeadersWithApiKey("pk_practice_1234567890");

        // Create payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dynamicEmail);
        payload.put("phone", "+919876543210");
        payload.put("firstName", "TestUser");
        payload.put("lastName", "Demo");
        payload.put("kycStatus", "pending");

        // Execute POST with fluent assertions
        ApiResponse response = ApiClient.post(requestContext, "/v1/users", payload, headers)
                .expectStatus(201)  // Fluent assertion for status code
                .expectSuccess();   // Fluent assertion for 2xx status

        // Parse response and extract user ID
        createdUserId = response.parseJson().getAsJsonObject("data").get("id").getAsString();
        LogUtils.info("User created successfully with ID: " + createdUserId);

        // Validate response contains expected fields
        response.validateFieldExists("data")
                .validateFieldExists("success");
    }

    @Test(priority = 2, dependsOnMethods = {"testPostRequestWithFluentApi"}, 
          description = "Demo - GET request and JSON parsing")
    public void testGetRequestWithJsonParsing() {
        LogUtils.info("=== Demo 2: GET Request with JSON Parsing ===");

        Map<String, String> headers = ApiClient.buildHeaders();

        // Execute GET request with fluent assertions
        ApiResponse response = ApiClient.get(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess()
                .printResponse();  // Print response for debugging

        // Parse JSON and validate specific fields
        var jsonResponse = response.parseJson();
        var userData = jsonResponse.getAsJsonObject("data");
        
        LogUtils.info("Retrieved user - Email: " + userData.get("email").getAsString());
        LogUtils.info("Retrieved user - FirstName: " + userData.get("firstName").getAsString());

        // Validate field values
        response.validateField("data", userData.toString());
    }

    @Test(priority = 3, dependsOnMethods = {"testPostRequestWithFluentApi"},
          description = "Demo - PUT request to update resource")
    public void testPutRequestUpdate() {
        LogUtils.info("=== Demo 3: PUT Request (Update Resource) ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey("pk_practice_1234567890");

        // Create update payload
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", dynamicEmail);
        updatePayload.put("phone", "+919999999999");
        updatePayload.put("firstName", "UpdatedUser");
        updatePayload.put("lastName", "Updated");
        updatePayload.put("kycStatus", "approved");

        // Execute PUT request
        ApiResponse response = ApiClient.put(
                requestContext, 
                "/v1/users/" + createdUserId, 
                updatePayload, 
                headers
        )
        .expectStatus(200)
        .printResponse();

        LogUtils.info("User updated successfully");
    }

    @Test(priority = 4, dependsOnMethods = {"testPostRequestWithFluentApi"},
          description = "Demo - DELETE request to remove resource")
    public void testDeleteRequestRemove() {
        LogUtils.info("=== Demo 4: DELETE Request (Remove Resource) ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey("pk_practice_1234567890");

        // Execute DELETE request
        ApiResponse response = ApiClient.delete(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess();

        LogUtils.info("User deleted successfully");
    }

    @Test(description = "Demo - Custom headers with Bearer token")
    public void testBearerTokenAuthentication() {
        LogUtils.info("=== Demo 5: Bearer Token Authentication ===");

        // Build headers with Bearer token
        Map<String, String> headers = ApiClient.buildHeadersWithBearerToken("your_token_here");
        headers = ApiClient.addHeader(headers, "X-Custom-Header", "CustomValue");

        LogUtils.info("Headers built with Bearer token and custom header");
        headers.forEach((key, value) -> LogUtils.info("Header - " + key + ": " + value));
    }

    @Test(description = "Demo - Form-encoded POST request")
    public void testFormEncodedPost() {
        LogUtils.info("=== Demo 6: Form-Encoded POST ===");

        // Build form data
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", "demo");
        formData.put("client_secret", "password123");

        Map<String, String> headers = ApiClient.buildHeaders();

        // Execute form-encoded POST (useful for OAuth2, login forms, etc.)
        ApiResponse response = ApiClient.postFormEncoded(
                requestContext,
                "/oauth/token",
                formData,
                headers
        );

        LogUtils.info("Form-encoded POST completed with status: " + response.getStatusCode());
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Cleaning up test environment...");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}

