package day7;

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

public class ApiIntegrationTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://reqres.in";
    private static String createdUserId; // Stores dynamic ID across test methods

    @BeforeClass
    public void setup() {
        LogUtils.info("Setting up API testing environment context.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);
    }

    @Test(priority = 1, description = "TC9 - Verify GET request retrieves active list data")
    public void testGetUsersList() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        // Mimic a real browser request to bypass proxy/firewall filtering
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        APIResponse response = ApiUtils.get(requestContext, "/api/users?page=2", headers);

        LogUtils.info("GET Response status received: " + response.status());
        Assert.assertEquals(response.status(), 200, "GET request did not return an OK status!");
    }

    @Test(priority = 2, description = "TC10 - Verify POST request creates a record successfully")
    public void testCreateUserRecord() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> bodyPayload = new HashMap<>();
        bodyPayload.put("name", "John Doe");
        bodyPayload.put("job", "QA Lead Architect");

        APIResponse response = ApiUtils.post(requestContext, "/api/users", bodyPayload, headers);

        LogUtils.info("POST Response status received: " + response.status());
        Assert.assertEquals(response.status(), 201, "POST request did not create resource status correctly!");

        String responseBody = response.text();
        LogUtils.debug("POST Response Body Content: " + responseBody);

        // Quick extraction concept (mock parsing) to grab the returned user identification hash token
        Assert.assertTrue(responseBody.contains("id"), "Response did not yield a valid user ID token!");

        // Poor-man's regex extractor just to isolate the ID field for subsequent test states without bulky third-party libraries
        createdUserId = responseBody.split("\"id\":\"")[1].split("\"")[0];
        LogUtils.info("Extracted generated resource tracking context pointer reference ID: " + createdUserId);
    }

    @Test(priority = 3, description = "TC11 - Verify PUT request maps data modifications safely")
    public void testUpdateUserRecord() {
        // Fallback to a static valid entity parameter index block if the prior dependent stage failed
        String targetId = (createdUserId != null) ? createdUserId : "2";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> updatedPayload = new HashMap<>();
        updatedPayload.put("name", "John Doe");
        updatedPayload.put("job", "Principal Engineering Director");

        APIResponse response = ApiUtils.put(requestContext, "/api/users/" + targetId, updatedPayload, headers);

        LogUtils.info("PUT Response status received: " + response.status());
        Assert.assertEquals(response.status(), 200, "PUT request modification operations dropped execution failure anomalies!");

        String responseBody = response.text();
        Assert.assertTrue(responseBody.contains("updatedAt"), "Payload execution timestamp parameters missing.");
    }

    @Test(priority = 4, description = "TC12 - Verify DELETE operations clean up targeting metrics securely")
    public void testRemoveUserRecord() {
        String targetId = (createdUserId != null) ? createdUserId : "2";

        APIResponse response = ApiUtils.delete(requestContext, "/api/users/" + targetId, null);

        LogUtils.info("DELETE Response status received: " + response.status());
        // ReqRes mock service layer safely structural rules define 204 No Content output sequences upon resource removals
        Assert.assertEquals(response.status(), 204, "Target removal sequence dropped error states!");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Closing active API request connection nodes.");

        // FIX: Swap context.close() with context.dispose()
        if (requestContext != null) {
            requestContext.dispose();
            LogUtils.debug("APIRequestContext successfully disposed.");
        }

        if (playwright != null) {
            playwright.close();
        }
    }
}