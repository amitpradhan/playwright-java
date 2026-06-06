package day11;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.reports.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiToUiUserTest {
    private Playwright playwright;
    private APIRequestContext requestContext;
    private Browser browser;
    private Page page;

    // Dynamic data variables generated at runtime
    private String dynamicEmail;
    private String dynamicPhone;
    private String dynamicFirstName;
    private String dynamicLastName;

    private final String uiUrl = "https://gauravkhurana.com/practise-api/ui/index.html#/users";

    @BeforeClass
    public void setup() {
        LogUtils.info("Initializing unified Playwright Engine for E2E API-to-UI verification.");
        playwright = Playwright.create();

        // 1. Initialize API Request Context
        requestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL("https://billpay-api.gauravkhurana-practice-api.workers.dev")
        );

        // 2. Initialize UI Browser Context (Headed mode so you can watch the validation live)
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();

        // 3. Generate completely random, dynamic dataset for this run execution
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        dynamicFirstName = "auto_" + uniqueId;
        dynamicLastName = "Kumar";
        dynamicEmail = "test_auto_" + System.currentTimeMillis() + "@example.com";
        dynamicPhone = "+919" + (long)(Math.random() * 100000000L);
    }

    @Test(priority = 1, description = "Step 1: Dynamically generate and create a user via backend API POST")
    public void testCreateUserViaApi() {
        LogUtils.info("Creating dynamic user via API POST: " + dynamicFirstName);

        // Build the dynamic payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", dynamicEmail);
        payload.put("phone", dynamicPhone);
        payload.put("firstName", dynamicFirstName);
        payload.put("lastName", dynamicLastName);
        payload.put("kycStatus", "pending");

        // Execute the POST request with chain-linked explicit headers
        APIResponse response = requestContext.post("/v1/users",
                RequestOptions.create()
                        .setHeader("X-API-Key", "pk_practice_1234567890") // Added individual key string
                        .setHeader("Content-Type", "application/json")      // Added content definition
                        .setHeader("Accept", "application/json")            // Added accept rule
                        .setData(payload)
        );

        LogUtils.info("API Response status received: " + response.status());
        Assert.assertTrue(response.status() == 201 || response.status() == 200,
                "API User creation failed! Body: " + response.text());

        // Parse and verify the API response structure matches our intent
        JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
        Assert.assertTrue(jsonResponse.get("success").getAsBoolean(), "API response envelope returned success=false");

        JsonObject dataObj = jsonResponse.getAsJsonObject("data");
        Assert.assertEquals(dataObj.get("firstName").getAsString(), dynamicFirstName, "First name mapping mismatched in database return!");

        LogUtils.info("API Verification Passed! User committed to database successfully.");
    }

//    @Test(priority = 2, dependsOnMethods = {"testCreateUserViaApi"}, description = "Step 2: Navigate to UI dashboard and validate data persistence")
//    public void testValidateUserInUi() {
//        LogUtils.info("Navigating browser to User management grid table interface: " + uiUrl);
//
//        // Go to the UI users page
//        page.navigate(uiUrl);
//
//        // Ensure the network layout completely settles down and table loads fully
//        page.waitForLoadState();
//
//        LogUtils.info("Searching for our dynamic first name within the live data table grid rows: " + dynamicFirstName);
//
//        // Targeting the table rows directly via 'tr' keeps the filter fast and highly accurate
//        Locator userRowElement = page.locator("tr").filter(
//                new Locator.FilterOptions().setHasText(dynamicFirstName)
//        );
//
//        // Explicitly assert that the dynamically injected data element is visibly rendered on screen
//        Assert.assertTrue(userRowElement.first().isVisible(),
//                "E2E Validation Failed! The dynamically created user [" + dynamicFirstName + "] was not found on the UI page.");
//
//        // Take a screenshot of the dashboard state for our historical log reports
//        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/user_created_validation.png")));
//
//        LogUtils.info("Success! Dynamic user created via API and verified inside the UI application layer smoothly.");
//    }



    @Test(priority = 2, dependsOnMethods = {"testCreateUserViaApi"}, description = "Step 2: Navigate to UI dashboard and validate data persistence")
    public void testValidateUserInUi() {
        LogUtils.info("Navigating browser to User management grid table interface: " + uiUrl);

        // Go to the UI users page
        page.navigate(uiUrl);

        // Ensure the network layout settled down
        page.waitForLoadState();

        LogUtils.info("Searching for our dynamic first name within the live data table grid rows: " + dynamicFirstName);

        // Target the row containing your dynamic first name
        Locator userRowElement = page.locator("tr").filter(
                new Locator.FilterOptions().setHasText(dynamicFirstName)
        );

        // FIX: Use Playwright's smart-waiting assertion instead of standard TestNG Assert.assertTrue
        // This will automatically poll the UI until the row actually shows up
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(userRowElement.first())
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(5000));

        // Take a screenshot of the dashboard state for historical log reports
        page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/user_created_validation.png")));

        LogUtils.info("Success! Dynamic user created via API and verified inside the UI application layer smoothly.");
    }

    @AfterClass
    public void tearDown() {
        LogUtils.info("Cleaning up testing execution engines.");
        if (page != null) page.close();
        if (browser != null) browser.close();
        if (requestContext != null) requestContext.dispose();
        if (playwright != null) playwright.close();
    }
}