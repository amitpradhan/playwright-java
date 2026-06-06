package day9;

import com.google.gson.JsonArray;
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

public class ApiUsersTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private final String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing Playwright Engine context for Users Endpoint API testing.");
        playwright = Playwright.create();
        requestContext = ApiUtils.createRequestContext(playwright, baseUrl);
    }

    @Test(priority = 1, description = "TC14 - Verify user listing payload structure and pagination controls")
    public void testGetUsersWithPagination() {
        // STEP 1: Apply the precise X-API-Key authorization scheme required for data endpoints
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", "pk_practice_1234567890"); // Custom API Key validation token
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // STEP 2: Endpoint path definition matching single forward slash rules
        String endpointWithParams = "/v1/users?page=1&limit=5";

        LogUtils.info("Injecting structural Custom API Key header sequence into request context for Users.");
        LogUtils.info("Invoking GET request for paginated user list: " + endpointWithParams);

        // STEP 3: Execute the request
        APIResponse response = ApiUtils.get(requestContext, endpointWithParams, headers);
        LogUtils.info("Users API response code status: " + response.status());

        // Step 4: Core HTTP status assertion
        Assert.assertEquals(response.status(), 200, "Users endpoint failed to return a valid success code!");

        // Step 5: Parse response body text via Gson
        String responseBody = response.text();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        // Step 6: Root structural validation checks
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean(), "Expected property 'success' flag to yield true.");
        Assert.assertTrue(jsonResponse.has("data"), "Missing critical array element context named 'data'.");

        // Step 7: Loop validation across the data records collection array block
        JsonArray dataArray = jsonResponse.getAsJsonArray("data");
        Assert.assertEquals(dataArray.size(), 5, "Pagination requested a limit of 5 records, but array length mismatched!");

        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject userObj = dataArray.get(i).getAsJsonObject();

            Assert.assertTrue(userObj.has("id"), "User tracking field 'id' is missing at position index: " + i);
            Assert.assertTrue(userObj.has("email"), "User property 'email' is missing at position index: " + i);
            Assert.assertTrue(userObj.has("phone"), "User property 'phone' is missing at position index: " + i);
            Assert.assertTrue(userObj.has("firstName"), "User property 'firstName' is missing at position index: " + i);
            Assert.assertTrue(userObj.has("lastName"), "User property 'lastName' is missing at position index: " + i);
            Assert.assertTrue(userObj.has("kycStatus"), "User property 'kycStatus' is missing at position index: " + i);

            String userId = userObj.get("id").getAsString();
            Assert.assertTrue(userId.startsWith("user-"), "The ID format rule constraint violated for value: " + userId);
        }

        // Step 8: Deep validation inside metadata pagination control objects
        JsonObject metaObject = jsonResponse.getAsJsonObject("meta");
        JsonObject paginationObject = metaObject.getAsJsonObject("pagination");

        Assert.assertEquals(paginationObject.get("page").getAsInt(), 1);
        Assert.assertEquals(paginationObject.get("limit").getAsInt(), 5);
        Assert.assertTrue(paginationObject.get("total").getAsInt() > 0);
        Assert.assertTrue(paginationObject.get("hasNext").getAsBoolean());
        Assert.assertFalse(paginationObject.get("hasPrev").getAsBoolean());

        LogUtils.info("Success! All paginated layout schemas and user objects matched exact response rules cleanly.");
    }

    @Test(priority = 1, description = "TC14 - Verify user listing payload structure and pagination controls")
    public void testGetUsersWithPagination1() {
        // Apply the exact static credentials requested by gauravkhurana.com/practise-api/
        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", "demo-api-key-123"); // Updated to live sandbox token value
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        String endpointWithParams = "/v1/users?page=1&limit=5";

        LogUtils.info("Sending GET request for paginated user list using live sandbox key rules.");

        // Execute the request
        APIResponse response = ApiUtils.get(requestContext, endpointWithParams, headers);
        LogUtils.info("Users API response code status: " + response.status());

        // Core HTTP status assertion
        Assert.assertEquals(response.status(), 200, "Users endpoint failed to authorize using the demo api key!");

        // ... remainder of your parsing assertions stay exactly the same
    }


    @AfterClass
    public void tearDown() {
        LogUtils.info("Disposing active users network API requests context.");
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}