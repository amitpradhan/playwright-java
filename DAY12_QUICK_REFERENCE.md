# Day12 ApiClientToUiTest - Quick Reference

## File Location
```
src/test/java/day12/ApiClientToUiTest.java
```

## Test Methods Overview

| # | Method | Priority | Depends On | Focus |
|---|--------|----------|----------|-------|
| 1 | `testCreateUserViaApiClient()` | 1 | None | Create user via API |
| 2 | `testGetUserViaApiClient()` | 2 | Test 1 | Read user from API |
| 3 | `testUpdateUserViaApiClient()` | 3 | Test 1 | Update user via API |
| 4 | `testListUsersViaApiClient()` | 4 | Test 1 | List all users |
| 5 | `testValidateUserInUiWithApiClient()` | 5 | Test 1 | Verify user in UI |
| 6 | `testDeleteUserViaApiClient()` | 6 | Test 1 | Delete user from API |
| 7 | `testCompleteCrudWorkflow()` | None | None | Complete CRUD cycle |

## ApiClient Features Used

### POST (Create)
```java
ApiClient.post(requestContext, "/v1/users", payload, headers)
    .expectStatus(201)
    .validateFieldExists("data");
```

### GET (Read)
```java
ApiClient.get(requestContext, "/v1/users/" + userId, headers)
    .expectSuccess()
    .validateFieldExists("data.email");
```

### PUT (Update)
```java
ApiClient.put(requestContext, "/v1/users/" + userId, updatePayload, headers)
    .expectStatus(200)
    .validateField("data.kycStatus", "approved");
```

### DELETE (Remove)
```java
ApiClient.delete(requestContext, "/v1/users/" + userId, headers)
    .expectSuccess();
```

### Headers
```java
// With API Key
Map<String, String> headers = ApiClient.buildHeadersWithApiKey(apiKey);

// Default (Content-Type, Accept, User-Agent)
headers = ApiClient.buildHeaders();

// With Bearer Token
headers = ApiClient.buildHeadersWithBearerToken(token);
```

### Response Parsing
```java
// Parse as JSON object
JsonObject json = response.parseJson();

// Parse as JSON element (for arrays)
JsonElement element = response.parseJsonElement();

// Get raw body
String body = response.getBody();

// Debug print
response.printResponse();
```

### Fluent Assertions
```java
response
    .expectStatus(200)              // Exact status
    .expectSuccess()                // 2xx range
    .validateFieldExists("data")    // Field exists
    .validateField("key", value)    // Field value matches
    .printResponse();               // For debugging
```

## Run Tests

### All ApiClientToUiTest tests
```bash
mvn test -Dtest=ApiClientToUiTest
```

### Single test method
```bash
mvn test -Dtest=ApiClientToUiTest#testCreateUserViaApiClient
```

### CRUD Workflow only
```bash
mvn test -Dtest=ApiClientToUiTest#testCompleteCrudWorkflow
```

### All Day12 tests
```bash
mvn test -Dtest=day12.*
```

## Key Differences from Original Test

**Original (ApiToUiUserTest):**
- Direct APIRequestContext calls
- Manual response assertions
- Manual JSON parsing
- 2 test methods

**Refactored (ApiClientToUiTest):**
- Fluent ApiClient wrapper
- Automatic assertions with fluent API
- Built-in JSON parsing
- 7 test methods with better coverage

## Test Flow Diagram

```
Setup (Playwright, APIContext, Browser)
  |
  +→ Test 1: Create User
  |    └─→ Test 2: Get User
  |    └─→ Test 3: Update User  
  |    └─→ Test 4: List Users
  |    └─→ Test 5: Validate in UI
  |    └─→ Test 6: Delete User
  |
  +→ Test 7: Complete CRUD Workflow (independent)
  |
Teardown (Close all resources)
```

## Dynamic Test Data Example

For each test run, unique data is generated:

```
FirstName: apiclient_a1b2c3d4    (8-char UUID suffix)
LastName: Automation
Email: apiclient_1718090000000@example.com    (timestamp)
Phone: +919876543210    (random 8-digit)
```

This ensures no conflicts with previous test runs.

## Expected Logs

```
Initializing unified Playwright environment for E2E API-to-UI testing
Generated dynamic test data:
  FirstName: apiclient_xyz12345
  LastName: Automation
  Email: apiclient_1718090000000@example.com
  Phone: +919876543210

TEST 1: Creating user via API with ApiClient
Executing: POST /v1/users with ApiClient.post()
✓ Status code validation passed: 201
✓ Success status validation passed
✓ Field existence validation passed: data
✓ Field existence validation passed: success
✓ Field validation passed: success = true
✓ User creation verified! Created user ID: abc123xyz

TEST 2: Retrieving user via API with ApiClient
✓ User data verified successfully!

TEST 3: Updating user via API with ApiClient
✓ User update verified! KYC status changed to approved

TEST 4: Listing users via API with ApiClient
✓ Found created user in list - Email: apiclient_1718090000000@example.com

TEST 5: Validating user in UI after API creation
Navigating to UI: https://gauravkhurana.com/practise-api/ui/index.html#/users
Searching for user with firstName: apiclient_xyz12345
✓ User found in UI table!
✓ Screenshot saved: target/day12_user_validation_apiclient_xyz12345.png

TEST 6: Deleting user via API with ApiClient
Executing: DELETE /v1/users/abc123xyz
✓ User deleted successfully via API

Tearing down test environment and cleanup resources
✓ Browser page closed
✓ Browser closed
✓ API request context disposed
✓ Playwright closed
```

## Files Involved

| File | Role |
|------|------|
| `ApiClientToUiTest.java` | Main refactored test class |
| `ApiClient.java` | Utility providing fluent API |
| `ApiToUiUserTest.java` | Original implementation (for reference) |
| `LogUtils.java` | Logging utility |
| `AuthTokenManager.java` | OAuth2 token management (if needed) |

## Import Statements

```java
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.annotations.*;
import utils.api.ApiClient;
import utils.api.ApiClient.ApiResponse;
import utils.reports.LogUtils;
import java.nio.file.Paths;
import java.util.*;
```

## Configuration Constants

```java
String baseUrl = "https://billpay-api.gauravkhurana-practice-api.workers.dev";
String apiKey = "pk_practice_1234567890";
String uiUrl = "https://gauravkhurana.com/practise-api/ui/index.html#/users";
```

## Success Criteria

✅ All 7 tests pass
✅ User created successfully
✅ User data retrieved and matches
✅ User updated successfully
✅ User appears in list
✅ User visible in UI table
✅ User deleted successfully
✅ CRUD workflow completes

## Notes

- Tests run in **headed mode** (visible browser) for observation
- Screenshots saved in `target/` directory
- Test data is unique per run (no conflicts)
- All resources properly cleaned up
- Fluent API is chainable for better readability

## Next Steps

1. Run the test: `mvn test -Dtest=ApiClientToUiTest`
2. Observe test execution in browser
3. Review logs in console
4. Check screenshots in `target/` directory
5. Extend with more test cases as needed

---

**Status:** ✅ Ready for Use
**Created:** June 11, 2026

