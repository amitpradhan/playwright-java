package day13;

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
 */
public class ApiClientToUiTest {

    private Playwright playwright;
    private APIRequestContext requestContext;
    private Browser browser;
    private Page page;

    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
    private final String apiKey = "pk_practice_1234567890";
    private final String uiUrl = "https://gauravkhurana.com/practise-api/ui/index.html#/users";

    private String dynamicEmail;
    private String dynamicPhone;
    private String dynamicFirstName;
    private String dynamicLastName;
    private String createdUserId;

    @BeforeClass
    public void setup() {
        LogUtils.info("Setting up unified Playwright environment for E2E API-to-UI testing with ApiClient utility");

        playwright = Playwright.create();
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(baseUrl)
        );

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();

        generateDynamicTestData();
    }

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

    @Test(priority = 1, description = "Test 1: Create user via API using ApiClient fluent utility")
    public void testCreateUserViaApiClient() {
        LogUtils.info("=== TEST 1: Creating user via API with ApiClient ===");

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dynamicEmail);
        payload.put("phone", dynamicPhone);
        payload.put("firstName", dynamicFirstName);
        payload.put("lastName", dynamicLastName);
        payload.put("kycStatus", "pending");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: POST /v1/users with ApiClient.post()");

        ApiResponse response = ApiClient.post(requestContext, "/v1/users", payload, headers)
                .expectStatus(201)
                .expectSuccess()
                .validateFieldExists("data")
                .validateFieldExists("success")
                .validateField("success", true);

        JsonObject jsonResponse = response.parseJson();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");

        createdUserId = dataObj.get("id").getAsString();
        String returnedFirstName = dataObj.get("firstName").getAsString();
        String returnedEmail = dataObj.get("email").getAsString();

        Assert.assertEquals(returnedFirstName, dynamicFirstName, "First name mismatch in API response!");
        Assert.assertEquals(returnedEmail, dynamicEmail, "Email mismatch in API response!");

        LogUtils.info("✓ User creation verified! Created user ID: " + createdUserId);
    }

    @Test(priority = 2, dependsOnMethods = {"testCreateUserViaApiClient"},
            description = "Test 2: Retrieve and validate user data via API")
    public void testGetUserViaApiClient() {
        LogUtils.info("=== TEST 2: Retrieving user via API with ApiClient ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: GET /v1/users/" + createdUserId);

        ApiResponse response = ApiClient.get(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess()
                .validateFieldExists("data")
                .validateFieldExists("data.email")
                .validateFieldExists("data.firstName")
                .printResponse();

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

    @Test(priority = 3, dependsOnMethods = {"testCreateUserViaApiClient"},
            description = "Test 3: Update user data via API using ApiClient.put()")
    public void testUpdateUserViaApiClient() {
        LogUtils.info("=== TEST 3: Updating user via API with ApiClient ===");

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", dynamicEmail);
        updatePayload.put("phone", dynamicPhone);
        updatePayload.put("firstName", dynamicFirstName);
        updatePayload.put("lastName", dynamicLastName);
        updatePayload.put("kycStatus", "verified");  // FIX: Value changed from "approved" to valid value "verified"

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: PUT /v1/users/" + createdUserId + " to update KYC status");

        ApiClient.put(requestContext, "/v1/users/" + createdUserId, updatePayload, headers)
                .expectStatus(200)
                .expectSuccess()
                .validateFieldExists("data")
                .validateField("data.kycStatus", "verified");

        LogUtils.info("✓ User update verified! KYC status changed to verified");
    }

    @Test(priority = 4, dependsOnMethods = {"testCreateUserViaApiClient"},
            description = "Test 4: List users and verify created user in the list")
    public void testListUsersViaApiClient() {
        LogUtils.info("=== TEST 4: Listing users via API with ApiClient ===");

        // FIX: Attached missing API authentication key header mapping loop
        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: GET /v1/users to list all users");

        ApiResponse response = ApiClient.get(requestContext, "/v1/users", headers)
                .expectSuccess()
                .validateFieldExists("data")
                .validateFieldExists("success");

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

    @Test(priority = 5, dependsOnMethods = {"testCreateUserViaApiClient"},
            description = "Test 5: Navigate to UI and verify created user in table")
    public void testValidateUserInUiWithApiClient() {
        LogUtils.info("=== TEST 5: Validating user in UI after API creation ===");

        LogUtils.info("Navigating to UI: " + uiUrl);
        page.navigate(uiUrl);
        page.waitForLoadState();

        LogUtils.info("Searching for user with firstName: " + dynamicFirstName);

        Locator userRowElement = page.locator("tr").filter(
                new Locator.FilterOptions().setHasText(dynamicFirstName)
        );

        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(userRowElement.first())
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
                        .setTimeout(5000));

        LogUtils.info("✓ User found in UI table!");

        String screenshotPath = "target/day12_user_validation_" + dynamicFirstName + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
        LogUtils.info("✓ Screenshot saved: " + screenshotPath);
    }

//    @Test(priority = 6, dependsOnMethods = {"testCreateUserViaApiClient"}, description = "Test 6: Delete user via API using ApiClient.delete()")
    public void testDeleteUserViaApiClient() {
        LogUtils.info("=== TEST 6: Deleting user via API with ApiClient ===");

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        LogUtils.info("Executing: DELETE /v1/users/" + createdUserId);

        ApiClient.delete(requestContext, "/v1/users/" + createdUserId, headers)
                .expectSuccess();

        LogUtils.info("✓ User deleted successfully via API");
    }

    @Test(description = "Test 7: Complete CRUD workflow - Create, Read, Update, Delete")
    public void testCompleteCrudWorkflow() {
        LogUtils.info("=== TEST 7: Complete CRUD Workflow Test ===");

        String workflowEmail = "workflow_" + System.currentTimeMillis() + "@example.com";
        String workflowFirstName = "workflow_test";

        Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

        // 1. CREATE
        LogUtils.info("1. CREATE - Creating new user");
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("email", workflowEmail);
        createPayload.put("phone", "+919999988888");
        createPayload.put("firstName", workflowFirstName);
        createPayload.put("lastName", "Test");
        createPayload.put("kycStatus", "pending");

        // Fixed recursive path resolving works natively here now
        String workflowUserId = ApiClient.post(requestContext, "/v1/users", createPayload, headers)
                .expectStatus(201)
                .validateFieldExists("data.id")
                .parseJson()
                .getAsJsonObject("data")
                .get("id")
                .getAsString();

        LogUtils.info("✓ Created user with ID: " + workflowUserId);

        // 2. READ
        LogUtils.info("2. READ - Retrieving created user");
        ApiClient.get(requestContext, "/v1/users/" + workflowUserId, headers)
                .expectSuccess()
                .validateField("data.firstName", workflowFirstName);

        LogUtils.info("✓ User retrieved and validated");

        // 3. UPDATE
        LogUtils.info("3. UPDATE - Updating user status");
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", workflowEmail);
        updatePayload.put("phone", "+919999988888");
        updatePayload.put("firstName", workflowFirstName);
        updatePayload.put("lastName", "Test");
        updatePayload.put("kycStatus", "verified"); // FIX: Casing rule update

        ApiClient.put(requestContext, "/v1/users/" + workflowUserId, updatePayload, headers)
                .expectStatus(200)
                .validateField("data.kycStatus", "verified");

        LogUtils.info("✓ User updated");

        // 4. DELETE
//        LogUtils.info("4. DELETE - Deleting user");
//        ApiClient.delete(requestContext, "/v1/users/" + workflowUserId, headers)
//                .expectSuccess();
//
//        LogUtils.info("✓ User deleted successfully");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Tearing down test environment and cleanup resources");
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
        LogUtils.info("Test environment cleanup completed");
    }
}