package day9;

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

public class ApiUserCrudTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";

    // Class-level state tracking variable to share the generated user ID across tests
    private static String targetUserId;
    private Map<String, String> authHeaders;

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing Playwright Engine context for full CRUD operations.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);

        // Prepare the reusable authorization headers required for data endpoints
        authHeaders = new HashMap<>();
        authHeaders.put("X-API-Key", "pk_practice_1234567890");
        authHeaders.put("Content-Type", "application/json");
        authHeaders.put("Accept", "application/json");
    }

    @Test(priority = 1, description = "CRUD - Create a new user record via POST")
    public void testCreateUser() {
        LogUtils.info("Executing: POST /v1/users");

        // Define user creation payload structure
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "automation_amit_" + System.currentTimeMillis() + "@example.com");
        payload.put("phone", "+919" + (long)(Math.random() * 100000000L));
        payload.put("firstName", "Amit");
        payload.put("lastName", "Automation");
        payload.put("kycStatus", "pending");

        APIResponse response = ApiUtils.post(requestContext, "/v1/users", payload, authHeaders);
        LogUtils.info("POST Response status code: " + response.status());

        // Assert resource created successfully (Accepting either 201 Created or 200 OK based on API contract)
        Assert.assertTrue(response.status() == 201 || response.status() == 200,
                "Expected successful creation status code, got: " + response.status());

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean());

        // Extract and capture the dynamic ID out of the target data object block
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        targetUserId = dataObj.get("id").getAsString();

        LogUtils.info("Successfully captured newly generated User ID: " + targetUserId);
        Assert.assertNotNull(targetUserId, "Captured User ID should not be null!");
    }

    @Test(priority = 2, dependsOnMethods = {"testCreateUser"}, description = "CRUD - Read and verify user details via GET")
    public void testReadUser() {
        LogUtils.info("Executing: GET /v1/users/" + targetUserId);

        APIResponse response = ApiUtils.get(requestContext, "/v1/users/" + targetUserId, authHeaders);
        LogUtils.info("GET Response status code: " + response.status());

        Assert.assertEquals(response.status(), 200, "Failed to fetch user record!");

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");

        // Validate baseline structural correctness of data persistence
        Assert.assertEquals(dataObj.get("id").getAsString(), targetUserId);
        Assert.assertEquals(dataObj.get("firstName").getAsString(), "Amit");
    }

    @Test(priority = 3, dependsOnMethods = {"testCreateUser"}, description = "CRUD - Update complete user details via PUT")
    public void testUpdateUserPut() {
        LogUtils.info("Executing: PUT /v1/users/" + targetUserId);

        // Put completely replaces the entity payload
        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("email", "updated_put_" + targetUserId + "@example.com");
        updatedPayload.put("phone", "+919999999999");
        updatedPayload.put("firstName", "AmitUpdated");
        updatedPayload.put("lastName", "PutChange");
        updatedPayload.put("kycStatus", "verified"); // Modifying status flag

        APIResponse response = ApiUtils.put(requestContext, "/v1/users/" + targetUserId, updatedPayload, authHeaders);
        LogUtils.info("PUT Response status code: " + response.status());

        Assert.assertEquals(response.status(), 200, "PUT update operation failed!");

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");

        Assert.assertEquals(dataObj.get("firstName").getAsString(), "AmitUpdated", "First name did not update via PUT!");
        Assert.assertEquals(dataObj.get("kycStatus").getAsString(), "verified", "KYC status field change failure!");
    }

    @Test(priority = 4, dependsOnMethods = {"testCreateUser"}, description = "CRUD - Partially update user details via PATCH")
    public void testPartialUpdateUserPatch() {
        LogUtils.info("Executing: PATCH /v1/users/" + targetUserId);

        // Patch modifies individual field variables without needing the full payload dictionary
        Map<String, Object> patchPayload = new HashMap<>();
        patchPayload.put("lastName", "PatchedName");

        // Since we don't have a generic patch method defined inside your original ApiUtils,
        // we can invoke the raw request context directly using our auth headers mapping
        com.microsoft.playwright.options.RequestOptions options = com.microsoft.playwright.options.RequestOptions.create();
        authHeaders.forEach(options::setHeader);
        options.setData(patchPayload);

        APIResponse response = requestContext.patch("/v1/users/" + targetUserId, options);
        LogUtils.info("PATCH Response status code: " + response.status());

        Assert.assertEquals(response.status(), 200, "PATCH partial modification operation failed!");

        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        JsonObject dataObj = jsonResponse.getAsJsonObject("data");

        // Confirm change while verifying prior untouched PUT parameters still remain persistent
        Assert.assertEquals(dataObj.get("lastName").getAsString(), "PatchedName", "Last name did not update via PATCH!");
        Assert.assertEquals(dataObj.get("firstName").getAsString(), "AmitUpdated", "Untargeted patch field was modified incorrectly!");
    }

    @Test(priority = 5, dependsOnMethods = {"testCreateUser"}, description = "CRUD - Delete user from database via DELETE")
    public void testDeleteUser() {
        LogUtils.info("Executing: DELETE /v1/users/" + targetUserId);

        APIResponse response = ApiUtils.delete(requestContext, "/v1/users/" + targetUserId, authHeaders);
        LogUtils.info("DELETE Response status code: " + response.status());

        // Accept either a 200 OK or 204 No Content based on back-end api design specs
        Assert.assertTrue(response.status() == 200 || response.status() == 204,
                "Deletion operation returned an unexpected response status: " + response.status());
    }

    @Test(priority = 6, dependsOnMethods = {"testDeleteUser"}, description = "CRUD - Confirm record is completely unreachable (404)")
    public void testVerifyUserNotFoundAfterDelete() {
        LogUtils.info("Executing verification: GET /v1/users/" + targetUserId + " (Expecting 404)");

        APIResponse response = ApiUtils.get(requestContext, "/v1/users/" + targetUserId, authHeaders);
        LogUtils.info("Post-Delete GET Response status code: " + response.status());

        // Validate that tracking node resource returns exactly a 404 Not Found error
        Assert.assertEquals(response.status(), 404,
                "Security vulnerability or synchronization anomaly detected! Record still accessible after explicit removal.");

        LogUtils.info("Success! Full-cycle CRUD pipeline validation operations completed flawlessly.");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Cleaning execution contexts for CRUD suite.");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
        ;
 }

}