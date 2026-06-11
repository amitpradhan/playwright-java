# Day12 - ApiClientToUiTest Documentation

## Overview

A comprehensive refactored E2E API-to-UI test class using the new **`ApiClient`** utility with fluent API design.

**File:** `src/test/java/day12/ApiClientToUiTest.java`

## Test Class Features

This test class demonstrates:
- ✅ Using `ApiClient` fluent API for REST operations
- ✅ Header building utilities (`buildHeadersWithApiKey()`)
- ✅ Fluent assertions (`expectStatus()`, `expectSuccess()`)
- ✅ JSON response parsing and validation
- ✅ Complete CRUD workflow (Create, Read, Update, Delete)
- ✅ UI navigation and element validation
- ✅ Screenshot capture for historical logs
- ✅ Dynamic test data generation

## Test Methods

### 1. `testCreateUserViaApiClient()` [Priority: 1]
**Description:** Create user via API using ApiClient fluent utility

**What it does:**
- Generates dynamic test data (email, phone, firstName, lastName)
- Uses `ApiClient.buildHeadersWithApiKey()` to build headers
- Executes `ApiClient.post()` with fluent assertions
- Validates response status (201)
- Extracts and validates user ID from response

**Code Example:**
```java
ApiResponse response = ApiClient.post(requestContext, "/v1/users", payload, headers)
    .expectStatus(201)
    .expectSuccess()
    .validateFieldExists("data")
    .validateField("success", true);
```

### 2. `testGetUserViaApiClient()` [Priority: 2]
**Description:** Retrieve and validate user data via API

**What it does:**
- Uses `ApiClient.get()` to retrieve created user
- Validates response with fluent assertions
- Parses JSON response
- Validates all fields match what was created

**Code Example:**
```java
ApiResponse response = ApiClient.get(requestContext, "/v1/users/" + createdUserId, headers)
    .expectSuccess()
    .validateFieldExists("data.email")
    .validateFieldExists("data.firstName")
    .printResponse();
```

### 3. `testUpdateUserViaApiClient()` [Priority: 3]
**Description:** Update user data via API using ApiClient.put()

**What it does:**
- Uses `ApiClient.put()` for update operation
- Changes KYC status from "pending" to "approved"
- Validates update with fluent assertions

**Code Example:**
```java
ApiResponse response = ApiClient.put(requestContext, "/v1/users/" + createdUserId, updatePayload, headers)
    .expectStatus(200)
    .validateField("data.kycStatus", "approved");
```

### 4. `testListUsersViaApiClient()` [Priority: 4]
**Description:** List users and verify created user in the list

**What it does:**
- Uses `ApiClient.get()` to fetch all users
- Parses JSON array response
- Searches for created user in the list
- Validates user is present

**Code Example:**
```java
ApiResponse response = ApiClient.get(requestContext, "/v1/users", headers)
    .expectSuccess()
    .validateFieldExists("data");

// Parse and search array
JsonObject jsonResponse = response.parseJson();
for (var userElement : jsonResponse.get("data").getAsJsonArray()) {
    if (user.get("id").getAsString().equals(createdUserId)) {
        userFound = true;
        break;
    }
}
```

### 5. `testValidateUserInUiWithApiClient()` [Priority: 5]
**Description:** Navigate to UI and verify created user appears in table

**What it does:**
- Navigates to UI dashboard
- Waits for page load
- Uses Playwright locators to find user in table
- Uses Playwright smart assertions for waiting
- Captures screenshot for validation report

**Code Example:**
```java
Locator userRowElement = page.locator("tr").filter(
    new Locator.FilterOptions().setHasText(dynamicFirstName)
);

com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(userRowElement.first())
    .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
        .setTimeout(5000));

page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
```

### 6. `testDeleteUserViaApiClient()` [Priority: 6]
**Description:** Delete user via API using ApiClient.delete()

**What it does:**
- Uses `ApiClient.delete()` to remove user
- Validates delete operation succeeds
- Cleans up test data

**Code Example:**
```java
ApiClient.delete(requestContext, "/v1/users/" + createdUserId, headers)
    .expectSuccess();
```

### 7. `testCompleteCrudWorkflow()`
**Description:** Complete CRUD workflow in a single test

**What it does:**
- Demonstrates all CRUD operations in sequence
- Shows fluent chaining across multiple operations
- Create user → Read user → Update user → Delete user
- All with fluent API and assertions

**Code Example:**
```java
// CREATE
String userId = ApiClient.post(requestContext, "/v1/users", createPayload, headers)
    .expectStatus(201)
    .validateFieldExists("data.id")
    .parseJson()
    .getAsJsonObject("data")
    .get("id")
    .getAsString();

// READ
ApiClient.get(requestContext, "/v1/users/" + userId, headers)
    .expectSuccess();

// UPDATE
ApiClient.put(requestContext, "/v1/users/" + userId, updatePayload, headers)
    .expectStatus(200);

// DELETE
ApiClient.delete(requestContext, "/v1/users/" + userId, headers)
    .expectSuccess();
```

## Key Improvements Over Original Test

| Feature | Original (ApiToUiUserTest) | Refactored (ApiClientToUiTest) |
|---------|---------------------------|-------------------------------|
| API Calls | Direct `APIResponse` | `ApiClient` with fluent API |
| Headers | Manual creation | `ApiClient.buildHeaders*()` |
| Assertions | Manual `Assert.assertEquals()` | Fluent `.expectStatus()` |
| JSON Parsing | Manual `JsonParser` | Built-in `.parseJson()` |
| Chaining | Not possible | Fluent method chaining |
| Code Readability | Good | Excellent |
| Response Validation | Verbose | Clean and concise |
| Test Methods | 2 (Create + Validate) | 7 (Complete CRUD + List + Workflow) |

## Test Execution

### Run All Tests in ApiClientToUiTest
```bash
mvn test -Dtest=ApiClientToUiTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=ApiClientToUiTest#testCreateUserViaApiClient
```

### Run Complete CRUD Workflow Test Only
```bash
mvn test -Dtest=ApiClientToUiTest#testCompleteCrudWorkflow
```

### Run Day12 Tests
```bash
mvn test -Dtest=day12.*
```

## Setup and Teardown

### Setup (@BeforeClass)
- Creates Playwright instance
- Initializes APIRequestContext with base URL
- Launches browser in headed mode for visual observation
- Generates dynamic test data

### Teardown (@AfterClass)
- Closes all resources gracefully
- Disposes API context
- Logs all cleanup operations

## Test Data

Dynamic test data is generated for each test run to avoid duplicates:

```java
String uniqueId = UUID.randomUUID().toString().substring(0, 8);
dynamicFirstName = "apiclient_" + uniqueId;
dynamicLastName = "Automation";
dynamicEmail = "apiclient_" + System.currentTimeMillis() + "@example.com";
dynamicPhone = "+919" + String.format("%08d", (long)(Math.random() * 100000000L));
```

Example:
- FirstName: `apiclient_a1b2c3d4`
- LastName: `Automation`
- Email: `apiclient_1718090000000@example.com`
- Phone: `+919876543210`

## Key ApiClient Features Used

### 1. HTTP Methods
```java
ApiClient.post(requestContext, endpoint, payload, headers)
ApiClient.get(requestContext, endpoint, headers)
ApiClient.put(requestContext, endpoint, payload, headers)
ApiClient.delete(requestContext, endpoint, headers)
```

### 2. Header Builders
```java
ApiClient.buildHeadersWithApiKey(apiKey)    // For API key auth
ApiClient.buildHeaders()                     // Default headers
ApiClient.buildHeadersWithBearerToken(token) // For token auth
```

### 3. Fluent Assertions
```java
.expectStatus(201)              // Exact status code
.expectSuccess()                // 2xx status
.validateFieldExists("data")    // Field existence
.validateField("key", value)    // Field value validation
```

### 4. Response Parsing
```java
.parseJson()                    // Parse as JsonObject
.parseJsonElement()             // Parse as JsonElement (arrays)
.getBody()                      // Get raw response body
.printResponse()                // Debug output
```

## Configuration

**Base URL:**
```java
String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
```

**API Key:**
```java
String apiKey = "pk_practice_1234567890";
```

**UI URL:**
```java
String uiUrl = "https://gauravkhurana.com/practise-api/ui/index.html#/users";
```

## Expected Behavior

1. ✅ All API calls succeed with appropriate status codes
2. ✅ Response data matches request payload
3. ✅ User appears in list after creation
4. ✅ User appears in UI table with correct data
5. ✅ Updates persist and are retrievable
6. ✅ Deletion removes user from system
7. ✅ Screenshots are captured for validation

## Logging

All operations are logged using `LogUtils`:

```
Setting up unified Playwright environment for E2E API-to-UI testing...
Generated dynamic test data:
  FirstName: apiclient_a1b2c3d4
  LastName: Automation
  Email: apiclient_1718090000000@example.com
  Phone: +919876543210

TEST 1: Creating user via API with ApiClient
Executing: POST /v1/users with ApiClient.post()
Executing: POST /v1/users (fluent assertions)
✓ Status code validation passed: 201 for POST /v1/users
✓ Success status validation passed for POST /v1/users
✓ Field existence validation passed: data
✓ Field validation passed: success = true
✓ User creation verified! Created user ID: xyz123

TEST 2: Retrieving user via API with ApiClient
✓ User data verified successfully!

... (more tests)

Tearing down test environment and cleanup resources
✓ Browser page closed
✓ Browser closed
✓ API request context disposed
✓ Playwright closed
```

## Comparison with Original Test

### Original Configuration
```java
// ApiToUiUserTest
requestContext = playwright.request().newContext(
    new APIRequest.NewContextOptions()
        .setBaseURL("https://billpay-api.gauravkhurana-practice-api.workers.dev")
);

// Direct API call
APIResponse response = requestContext.post("/v1/users",
    RequestOptions.create()
        .setHeader("X-API-Key", "pk_practice_1234567890")
        .setHeader("Content-Type", "application/json")
        .setData(payload)
);

// Manual response checking
Assert.assertTrue(response.status() == 201 || response.status() == 200);
JsonObject jsonResponse = JsonParser.parseString(response.text()).getAsJsonObject();
```

### New Configuration
```java
// ApiClientToUiTest
requestContext = playwright.request().newContext(
    new APIRequest.NewContextOptions().setBaseURL(baseUrl)
);

// Fluent API call
ApiResponse response = ApiClient.post(requestContext, "/v1/users", payload, headers)
    .expectStatus(201)
    .expectSuccess()
    .validateField("success", true);

// Built-in response parsing
JsonObject jsonResponse = response.parseJson();
```

## Additional Test Methods

The refactored test includes additional methods not in the original:

1. **`testGetUserViaApiClient()`** - Dedicated GET test
2. **`testUpdateUserViaApiClient()`** - PUT/UPDATE operation
3. **`testListUsersViaApiClient()`** - List and search operations
4. **`testCompleteCrudWorkflow()`** - Full workflow example

## Benefits

✅ **Cleaner Code** - Fluent API reduces boilerplate
✅ **Better Readability** - Method names are self-documenting
✅ **Faster Testing** - Reusable header builders
✅ **Easier Maintenance** - Centralized API logic
✅ **Better Assertions** - Fluent assertions are more intuitive
✅ **More Test Cases** - Easy to add more tests
✅ **Better Logging** - Integrated with LogUtils

## Related Files

- **Main Utility:** `src/main/java/utils/api/ApiClient.java`
- **Original Implementation:** `src/test/java/day11/ApiToUiUserTest.java`
- **Example Test:** `src/test/java/day12/ApiClientDemoTest.java`
- **Documentation:** `API_CLIENT_GUIDE.md`, `MIGRATION_GUIDE.md`

## Status

✅ **Complete and Production Ready**
✅ **All Tests Compile Successfully**
✅ **Ready for Execution**

---

**Last Updated:** June 11, 2026

