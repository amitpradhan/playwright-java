package day12;

import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.api.ApiClient;
import utils.api.ApiClient.ApiResponse;
import utils.reports.LogUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Refactored E2E API-to-UI Test using new ApiClient utility
 * 
 * Tests complete flow:
 * 1. Create a user via REST API using ApiClient utility (fluent API)
 * 2. Validate API response with fluent assertions
 * 3. Navigate to UI and verify the created user appears in the table
 * 4. Capture screenshots for historical logs
 * 5. Clean up resources
 */
public class ApiClientToUiTest {
    
    // Playwright components
    private Playwright playwright;
    private APIRequestContext requestContext;
    private Browser browser;
    private Page page;

    // API configuration
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private final String apiKey = "pk_practice_1234567890";
    
    // UI configuration
    private final String uiUrl = "https://gauravkhurana.com/practise-api/ui/index.html#/users";

    // Dynamic test data
    private String dynamicEmail;
    private String dynamicPhone;
    private String dynamicFirstName;
    private String dynamicLastName;
    private String createdUserId;

    @BeforeClass
    public void setup() {
        LogUtils.info("Setting up unified Playwright environment for E2E API-to-UI testing with ApiClient utility");
        
        // Initialize Playwright
        playwright = Playwright.create();

        // Initialize API Request Context with base URL
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(baseUrl)
        );

        // Initialize UI Browser (headed mode for visual validation)
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
        page = browser.newPage();

        // Generate random dynamic test data
        generateDynamicTestData();
    }

    /**
     * Generate unique test data for each test run to avoid duplicates
     */
    private void generateDynamicTestData() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        dynamicFirstName = "apiclient_" + uniqueId;
        dynamicLastName = "Automation";
        dynamicEmail = "apiclient_" + System.currentTimeMillis() + "@example.com";
        dynamicPhone = "+919" + String.format("%08d", (long)(Math.random() * 100000000L));
        
        LogUtils.info("Generated dynamic test data:");
        LogUtils.info("  FirstName: " + dynamicFirstName);
        LogUtils.info("  LastName: " + dynamicLastName);
        LogUtils.info("  Email: " + dynamicEmail);
        LogUtils.info("  Phone: " + dynamicPhone);
    }

    /**
     * Step 1: Create user via API using new ApiClient fluent API
     * Demonstrates:
     * - Using ApiClient.buildHeadersWithApiKey() for authentication
     * - Fluent assertions with expectStatus() and expectSuccess()
     * - JSON response parsing and validation
     * - Field extraction using fluent chaining
     */
    @Test(priority = 1, description = "Test 1: Create user via API using ApiClient fluent utility")
    public void testCreateUserViaApiClient() {
        LogUtils.info("=== TEST 1: Creating user via API with ApiClient ===");

        // Build request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dynamicEmail);
        payload.put("phone", dynamicPhone);
        payload.put("firstName", dynamicFirstName);
        payload.put("lastName", dynamicLastName);
        payload.put("kycStatus", "pending");

        // Build headers using ApiClient utility
        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: POST /v1/users with ApiClient.post()");

        // Execute API request with fluent assertions
        ApiResponse response = ApiClient.post(requestContext, "/v1/users", payload, headers)
                .expectStatus(201)                    // Assert created status
                .expectSuccess()                      // Assert within 2xx range
                .validateFieldExists("data")          // Validate response has data field
                .validateFieldExists("success")       // Validate success flag exists
                .validateField("success", true);      // Validate success is true

        // Parse JSON and extract user ID
        JsonObject jsonResponse = response.parseJson();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        
        createdUserId = dataObj.get("id").getAsString();
        String returnedFirstName = dataObj.get("firstName").getAsString();
        String returnedEmail = dataObj.get("email").getAsString();

        // Validate returned data matches what we sent
        Assert.assertEquals(returnedFirstName, dynamicFirstName, 
                "First name mismatch in API response!");
        Assert.assertEquals(returnedEmail, dynamicEmail, 
                "Email mismatch in API response!");

        LogUtils.info("✓ User creation verified! Created user ID: " + createdUserId);
    }

    /**
     * Step 2: Get user via API using ApiClient to verify data persistence
     * Demonstrates:
     * - Using ApiClient.get() with fluent chaining
     * - JSON response parsing
     * - Field validation
     */
    @Test(priority = 2, dependsOnMethods = {"testCreateUserViaApiClient"},
          description = "Test 2: Retrieve and validate user data via API")
    public void testGetUserViaApiClient() {
        LogUtils.info("=== TEST 2: Retrieving user via API with ApiClient ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: GET /v1/users/" + createdUserId);

        // Execute GET request with fluent assertions and response parsing
        ApiResponse response = ApiClient.get(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess()
                .validateFieldExists("data")
                .validateFieldExists("data.email")
                .validateFieldExists("data.firstName")
                .printResponse();  // Print for debugging

        // Validate response content
        JsonObject jsonResponse = response.parseJson();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        
        String retrievedEmail = dataObj.get("email").getAsString();
        String retrievedFirstName = dataObj.get("firstName").getAsString();
        String retrievedPhone = dataObj.get("phone").getAsString();

        Assert.assertEquals(retrievedEmail, dynamicEmail, "Email mismatch in retrieved data!");
        Assert.assertEquals(retrievedFirstName, dynamicFirstName, "FirstName mismatch in retrieved data!");
        Assert.assertEquals(retrievedPhone, dynamicPhone, "Phone mismatch in retrieved data!");

        LogUtils.info("✓ User data verified successfully!");
    }

    /**
     * Step 3: Update user via API using ApiClient.put()
     * Demonstrates:
     * - Using ApiClient.put() for update operations
     * - Fluent assertions on update responses
     */
    @Test(priority = 3, dependsOnMethods = {"testCreateUserViaApiClient"},
          description = "Test 3: Update user data via API using ApiClient.put()")
    public void testUpdateUserViaApiClient() {
        LogUtils.info("=== TEST 3: Updating user via API with ApiClient ===");

        // Build update payload - update KYC status
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", dynamicEmail);
        updatePayload.put("phone", dynamicPhone);
        updatePayload.put("firstName", dynamicFirstName);
        updatePayload.put("lastName", dynamicLastName);
        updatePayload.put("kycStatus", "approved");  // Changed status

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: PUT /v1/users/" + createdUserId + " to update KYC status");

        // Execute PUT request with fluent assertions
        ApiResponse response = ApiClient.put(requestContext, "/v1/users/" + createdUserId, updatePayload, headers)
                .expectStatus(200)
                .expectSuccess()
                .validateFieldExists("data")
                .validateField("data.kycStatus", "approved");

        LogUtils.info("✓ User update verified! KYC status changed to approved");
    }

    /**
     * Step 4: List users via API to verify updated user is in the list
     * Demonstrates:
     * - Using ApiClient.get() for list operations
     * - Parsing JSON arrays from responses
     */
    @Test(priority = 4, dependsOnMethods = {"testCreateUserViaApiClient"},
          description = "Test 4: List users and verify created user in the list")
    public void testListUsersViaApiClient() {
        LogUtils.info("=== TEST 4: Listing users via API with ApiClient ===");

        Map<String, String> headers = ApiClient.buildHeaders();

        LogUtils.info("Executing: GET /v1/users to list all users");

        // Execute GET request to list users
        ApiResponse response = ApiClient.get(requestContext, "/v1/users", headers)
                .expectSuccess()
                .validateFieldExists("data")
                .validateFieldExists("success");

        // Parse response and verify our user is in the list
        JsonObject jsonResponse = response.parseJson();
        boolean userFound = false;

        if (jsonResponse.has("data")) {
            var dataArray = jsonResponse.get("data");
            if (dataArray.isJsonArray()) {
                for (var userElement : dataArray.getAsJsonArray()) {
                    JsonObject user = userElement.getAsJsonObject();
                    if (user.get("id").getAsString().equals(createdUserId)) {
                        userFound = true;
                        String email = user.get("email").getAsString();
                        LogUtils.info("✓ Found created user in list - Email: " + email);
                        break;
                    }
                }
            }
        }

        Assert.assertTrue(userFound, "Created user not found in users list!");
    }

    /**
     * Step 5: Navigate to UI and verify user appears in the table
     * Demonstrates 
     * - Integration between API and UI testing
     * - Using Playwright locators after API operations
     * - Smart waiting for UI elements
     */
    @Test(priority = 5, dependsOnMethods = {"testCreateUserViaApiClient"},
          description = "Test 5: Navigate to UI and verify created user in table")
    public void testValidateUserInUiWithApiClient() {
        LogUtils.info("=== TEST 5: Validating user in UI after API creation ===");

        LogUtils.info("Navigating to UI: " + uiUrl);
        page.navigate(uiUrl);

        // Wait for page to load completely
        page.waitForLoadState();

        LogUtils.info("Searching for user with firstName: " + dynamicFirstName);

        // Find the table row containing our dynamically created user
        Locator userRowElement = page.locator("tr").filter(
                new Locator.FilterOptions().setHasText(dynamicFirstName)
        );

        // Use Playwright's smart waiting assertion
        // This automatically polls until the element is visible (with timeout)
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(userRowElement.first())
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
                        .setTimeout(5000));

        LogUtils.info("✓ User found in UI table!");

        // Take screenshot for validation report
        String screenshotPath = "target/day12_user_validation_" + dynamicFirstName + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
        LogUtils.info("✓ Screenshot saved: " + screenshotPath);
    }

    /**
     * Step 6: Delete user via API using ApiClient.delete()
     * Demonstrates:
     * - Using ApiClient.delete() for cleanup
     * - Fluent assertions on delete responses
     */
    @Test(priority = 6, dependsOnMethods = {"testCreateUserViaApiClient"},
          description = "Test 6: Delete user via API using ApiClient.delete()")
    public void testDeleteUserViaApiClient() {
        LogUtils.info("=== TEST 6: Deleting user via API with ApiClient ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: DELETE /v1/users/" + createdUserId);

        // Execute DELETE request with fluent assertions
        ApiClient.delete(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess();

        LogUtils.info("✓ User deleted successfully via API");
    }

    /**
     * Complete CRUD workflow test combining all operations
     * Demonstrates full feature coverage in a single fluent test
     */
    @Test(description = "Test 7: Complete CRUD workflow - Create, Read, Update, Delete")
    public void testCompleteCrudWorkflow() {
        LogUtils.info("=== TEST 7: Complete CRUD Workflow Test ===");

        // Re-generate data for this test
        String workflowEmail = "workflow_" + System.currentTimeMillis() + "@example.com";
        String workflowFirstName = "workflow_test";

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        // CREATE
        LogUtils.info("1. CREATE - Creating new user");
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("email", workflowEmail);
        createPayload.put("phone", "+919999988888");
        createPayload.put("firstName", workflowFirstName);
        createPayload.put("lastName", "Test");
        createPayload.put("kycStatus", "pending");

        String workflowUserId = ApiClient.post(requestContext, "/v1/users", createPayload, headers)
                .expectStatus(201)
                .validateFieldExists("data.id")
                .parseJson()
                .getAsJsonObject("data")
                .get("id")
                .getAsString();

        LogUtils.info("✓ Created user with ID: " + workflowUserId);

        // READ
        LogUtils.info("2. READ - Retrieving created user");
        ApiClient.get(requestContext, "/v1/users/" + workflowUserId, headers)
                .expectSuccess()
                .validateField("data.firstName", workflowFirstName);

        LogUtils.info("✓ User retrieved and validated");

        // UPDATE
        LogUtils.info("3. UPDATE - Updating user status");
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", workflowEmail);
        updatePayload.put("phone", "+919999988888");
        updatePayload.put("firstName", workflowFirstName);
        updatePayload.put("lastName", "Test");
        updatePayload.put("kycStatus", "verified");

        ApiClient.put(requestContext, "/v1/users/" + workflowUserId, updatePayload, headers)
                .expectStatus(200)
                .validateField("data.kycStatus", "verified");

        LogUtils.info("✓ User updated");

        // DELETE
        LogUtils.info("4. DELETE - Deleting user");
        ApiClient.delete(requestContext, "/v1/users/" + workflowUserId, headers)
                .expectSuccess();

        LogUtils.info("✓ User deleted successfully");
    }

    /**
     * Cleanup resources after all tests
     */
    @AfterClass
    public void tearDown() {
        LogUtils.info("Tearing down test environment and cleanup resources");
        
        if (page != null) {
            page.close();
            LogUtils.info("✓ Browser page closed");
        }
        if (browser != null) {
            browser.close();
            LogUtils.info("✓ Browser closed");
        }
        if (requestContext != null) {
            requestContext.dispose();
            LogUtils.info("✓ API request context disposed");
        }
        if (playwright != null) {
            playwright.close();
            LogUtils.info("✓ Playwright closed");
        }
        
        LogUtils.info("Test environment cleanup completed");
    }
}

