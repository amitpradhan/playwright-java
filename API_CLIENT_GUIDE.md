# ApiClient - Generic REST API Utility Guide

## Overview

`ApiClient` is a comprehensive generic utility class for testing REST APIs using Playwright's APIRequestContext. It provides:

- **Generic HTTP Methods**: POST, GET, PUT, DELETE, PATCH
- **Fluent API Design**: Chainable methods for assertions and response handling
- **JSON Response Parsing**: Built-in JSON parsing and validation
- **Authentication Helpers**: Pre-built header builders for different auth schemes
- **Better Error Handling**: Detailed logging and meaningful error messages
- **Response Validation**: Fluent assertions for status codes and response fields

## Location

```
Package: utils.api
Class: ApiClient
File: src/main/java/utils/api/ApiClient.java
```

## Features

### 1. HTTP Methods

All HTTP methods follow the same pattern with optional custom headers and fluent return types.

#### GET Request
```java
Map<String, String> headers = ApiClient.buildHeaders();
ApiClient.get(requestContext, "/users/1", headers)
    .expectStatus(200)
    .printResponse();
```

#### POST Request (JSON)
```java
Map<String, Object> payload = new HashMap<>();
payload.put("name", "John");
payload.put("email", "john@example.com");

Map<String, String> headers = ApiClient.buildHeadersWithApiKey("api_key_123");

ApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .validateFieldExists("data");
```

#### POST Request (Form-Encoded)
```java
Map<String, String> formData = new HashMap<>();
formData.put("grant_type", "client_credentials");
formData.put("client_id", "demo");
formData.put("client_secret", "password123");

ApiClient.postFormEncoded(requestContext, "/oauth/token", formData, null)
    .expectStatus(200);
```

#### PUT Request (Update)
```java
Map<String, Object> updatePayload = new HashMap<>();
updatePayload.put("name", "JohnUpdated");

ApiClient.put(requestContext, "/users/1", updatePayload, headers)
    .expectStatus(200);
```

#### PATCH Request (Partial Update)
```java
Map<String, Object> patchPayload = new HashMap<>();
patchPayload.put("status", "active");

ApiClient.patch(requestContext, "/users/1", patchPayload, headers)
    .expectSuccess();
```

#### DELETE Request
```java
ApiClient.delete(requestContext, "/users/1", headers)
    .expectSuccess();
```

### 2. Response Wrapper (ApiResponse Class)

All HTTP methods return an `ApiResponse` object which provides:

#### Status Code Assertions
```java
.expectStatus(200)           // Exact status code
.expectSuccess()             // Expects 2xx status (200-299)
```

#### Response Body Operations
```java
.getStatusCode()             // Returns int status code
.getBody()                   // Returns response as String
.parseJson()                 // Returns response as JsonObject
.parseJsonElement()          // Returns response as JsonElement (for arrays)
```

#### Field Validation
```java
.validateFieldExists("data.user.id")     // Check if field exists
.validateField("status", "active")       // Validate field has specific value
```

#### Debugging
```java
.printResponse()             // Print full response for debugging
.getRawResponse()            // Get raw Playwright APIResponse
```

### 3. Header Builder Utilities

#### Default Headers
```java
Map<String, String> headers = PlaywrightApiClient.buildHeaders();
// Includes: Content-Type, Accept, User-Agent
```

#### API Key Authentication
```java
Map<String, String> headers = PlaywrightApiClient.buildHeadersWithApiKey("pk_practice_1234567890");
```

#### Bearer Token Authentication
```java
Map<String, String> headers = PlaywrightApiClient.buildHeadersWithBearerToken("your_jwt_token");
```

#### Custom Authorization
```java
Map<String, String> headers = PlaywrightApiClient.buildHeadersWithAuth("Bearer", "token_value");
```

#### Add Custom Headers
```java
Map<String, String> headers = PlaywrightApiClient.buildHeaders();
headers = PlaywrightApiClient.addHeader(headers, "X-Custom-Header", "CustomValue");
```

## Usage Examples

### Example 1: Create User and Validate Response

```java
public void testCreateUser() {
    // Setup
    Map<String, String> headers = ApiClient.buildHeadersWithApiKey("pk_practice_1234567890");
    
    // Create payload
    Map<String, Object> payload = new HashMap<>();
    payload.put("email", "test@example.com");
    payload.put("firstName", "John");
    payload.put("lastName", "Doe");
    
    // Execute and assert
    ApiClient.post(requestContext, "/v1/users", payload, headers)
        .expectStatus(201)                           // Assert created
        .validateFieldExists("data")                 // Check response has data field
        .validateField("success", true);             // Validate success flag
    
    LogUtils.info("User creation test passed!");
}
```

### Example 2: Full CRUD Workflow with Fluent Chain

```java
public void testCrudWorkflow() {
    Map<String, String> headers = ApiClient.buildHeadersWithApiKey("api_key");
    
    // CREATE
    ApiResponse createResponse = ApiClient.post(
        requestContext, "/users", createPayload, headers
    ).expectStatus(201);
    
    String userId = createResponse.parseJson()
        .getAsJsonObject("data")
        .get("id").getAsString();
    
    // READ
    ApiClient.get(requestContext, "/users/" + userId, headers)
        .expectSuccess()
        .validateFieldExists("data.email");
    
    // UPDATE
    ApiClient.put(requestContext, "/users/" + userId, updatePayload, headers)
        .expectStatus(200);
    
    // DELETE
    ApiClient.delete(requestContext, "/users/" + userId, headers)
        .expectSuccess();
}
```

### Example 3: Error Handling with Try-Catch

```java
public void testWithErrorHandling() {
    try {
        ApiClient.get(requestContext, "/users/999", headers)
            .expectStatus(200);  // This will throw if status is not 200
    } catch (AssertionError e) {
        LogUtils.info("Expected assertion error: " + e.getMessage());
    } catch (RuntimeException e) {
        LogUtils.error("Request failed", e);
    }
}
```

### Example 4: OAuth2 Token Exchange

```java
public void testOAuth2Flow() {
    Map<String, String> formData = new HashMap<>();
    formData.put("grant_type", "client_credentials");
    formData.put("client_id", "demo");
    formData.put("client_secret", "password123");
    
    ApiResponse response = ApiClient.postFormEncoded(
        requestContext,
        "/oauth/token",
        formData,
        null
    ).expectStatus(200);
    
    String token = response.parseJson().get("access_token").getAsString();
    
    // Use token for authenticated requests
    Map<String, String> authHeaders = ApiClient.buildHeadersWithBearerToken(token);
    
    ApiClient.get(requestContext, "/v1/auth/me", authHeaders)
        .expectSuccess();
}
```

### Example 5: Response Parsing and Iteration

```java
public void testResponseParsing() {
    ApiResponse response = ApiClient.get(requestContext, "/users", headers)
        .expectSuccess();
    
    // Parse as JSON
    JsonObject json = response.parseJson();
    
    // Access nested fields
    JsonObject userData = json.getAsJsonObject("data");
    String email = userData.get("email").getAsString();
    
    // Parse as array
    JsonElement arrayElement = response.parseJsonElement();
    if (arrayElement.isJsonArray()) {
        arrayElement.getAsJsonArray().forEach(element -> {
            LogUtils.info("Item: " + element.toString());
        });
    }
}
```

## Integration with Existing Code

The `PlaywrightApiClient` works seamlessly with existing utilities:

### With AuthTokenManager
```java
String token = AuthTokenManager.getValidToken(requestContext);
Map<String, String> headers = PlaywrightApiClient.buildHeadersWithBearerToken(token);

PlaywrightApiClient.get(requestContext, "/secured/data", headers)
    .expectSuccess();
```

### With ApiUtils (Original)
Both utilities can coexist. `PlaywrightApiClient` provides enhanced features:

```java
// Original approach (still works)
APIResponse response = ApiUtils.post(requestContext, "/users", payload, headers);

// Enhanced approach with fluent API
PlaywrightApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .validateFieldExists("data");
```

## Method Reference

### Instance Methods (ApiResponse)

| Method | Returns | Description |
|--------|---------|-------------|
| `getStatusCode()` | `int` | Get HTTP status code |
| `getBody()` | `String` | Get response body as string |
| `parseJson()` | `JsonObject` | Parse response as JSON object |
| `parseJsonElement()` | `JsonElement` | Parse response as JSON element |
| `expectStatus(int)` | `ApiResponse` | Assert specific status code |
| `expectSuccess()` | `ApiResponse` | Assert 2xx status code |
| `validateField(path, value)` | `ApiResponse` | Assert field value |
| `validateFieldExists(path)` | `ApiResponse` | Assert field exists |
| `printResponse()` | `ApiResponse` | Print response for debugging |
| `getRawResponse()` | `APIResponse` | Get raw Playwright response |

### Static Methods (PlaywrightApiClient)

| Method | Returns | Description |
|--------|---------|-------------|
| `get(ctx, endpoint, headers)` | `ApiResponse` | GET request |
| `post(ctx, endpoint, payload, headers)` | `ApiResponse` | POST request |
| `postFormEncoded(ctx, endpoint, data, headers)` | `ApiResponse` | Form-encoded POST |
| `put(ctx, endpoint, payload, headers)` | `ApiResponse` | PUT request |
| `patch(ctx, endpoint, payload, headers)` | `ApiResponse` | PATCH request |
| `delete(ctx, endpoint, headers)` | `ApiResponse` | DELETE request |
| `buildHeaders()` | `Map<String,String>` | Default headers |
| `buildHeadersWithApiKey(key)` | `Map<String,String>` | Headers with API key |
| `buildHeadersWithBearerToken(token)` | `Map<String,String>` | Headers with Bearer token |
| `buildHeadersWithAuth(scheme, value)` | `Map<String,String>` | Headers with custom auth |
| `addHeader(headers, key, value)` | `Map<String,String>` | Add header to map |

## Best Practices

1. **Always specify expected status codes** - Use `expectStatus()` or `expectSuccess()`
2. **Chain assertions** - Use fluent API for cleaner code
3. **Parse JSON properly** - Use `parseJson()` for objects, `parseJsonElement()` for arrays
4. **Use header builders** - Avoid manual header creation when possible
5. **Log responses** - Use `printResponse()` during debugging
6. **Test error cases** - Test both success and failure scenarios
7. **Reuse context** - Don't create multiple APIRequestContext objects

## Troubleshooting

### JSON Parsing Errors
```java
// Wrong: Response is an array, not an object
JsonObject json = response.parseJson();  // Throws error

// Correct: Use parseJsonElement for arrays
JsonElement element = response.parseJsonElement();
JsonArray arr = element.getAsJsonArray();
```

### Headers Not Being Sent
```java
// Correct: Pass headers to the method
PlaywrightApiClient.get(requestContext, "/endpoint", headers)

// Better: Use builder methods
Map<String, String> headers = PlaywrightApiClient.buildHeadersWithApiKey("key");
```

### Status Code Mismatches
```java
// Add debugging
PlaywrightApiClient.get(requestContext, "/users", headers)
    .printResponse()  // Print before assertion
    .expectStatus(200);
```

## Testing the Utility

Run the demo test to see all features in action:

```bash
mvn test -Dtest=ApiClientDemoTest
```

Or run tests in the day11 package:

```bash
mvn test -Dtest=day11.*
```

## Related Classes

- `ApiUtils.java` - Original generic utility with basic methods
- `AuthTokenManager.java` - OAuth2 token management
- `LogUtils.java` - Logging with Extent Reports integration

