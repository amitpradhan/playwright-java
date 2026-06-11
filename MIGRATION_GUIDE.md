# PlaywrightApiClient - Quick Reference & Migration Guide

## File Structure

```
src/main/java/utils/
├── ApiUtils.java                          (Original - Basic CRUD methods)
├── LinkUtils.java
├── api/
│   ├── PlaywrightApiClient.java          (NEW - Enhanced with fluent API)
│   └── AuthTokenManager.java
├── reports/
│   ├── LogUtils.java
│   └── ExtentReportListener.java
└── genericUtils/
```

## Comparison: Old vs New

### 1. GET Request

**Old Approach (ApiUtils)**
```java
Map<String, String> headers = new HashMap<>();
headers.put("Accept", "application/json");

APIResponse response = ApiUtils.get(requestContext, "/users/1", headers);
Assert.assertEquals(response.status(), 200);
String body = response.text();
```

**New Approach (PlaywrightApiClient)**
```java
Map<String, String> headers = PlaywrightApiClient.buildHeaders();

PlaywrightApiClient.get(requestContext, "/users/1", headers)
    .expectStatus(200)
    .printResponse();
```

### 2. POST Request

**Old Approach (ApiUtils)**
```java
Map<String, Object> payload = new HashMap<>();
payload.put("name", "John");
payload.put("email", "john@example.com");

Map<String, String> headers = new HashMap<>();
headers.put("X-API-Key", "pk_practice_1234567890");
headers.put("Content-Type", "application/json");

APIResponse response = ApiUtils.post(requestContext, "/users", payload, headers);
Assert.assertTrue(response.status() == 201 || response.status() == 200);

JsonObject json = JsonParser.parseString(response.text()).getAsJsonObject();
String id = json.getAsJsonObject("data").get("id").getAsString();
```

**New Approach (PlaywrightApiClient)**
```java
Map<String, Object> payload = new HashMap<>();
payload.put("name", "John");
payload.put("email", "john@example.com");

Map<String, String> headers = PlaywrightApiClient.buildHeadersWithApiKey("pk_practice_1234567890");

String id = PlaywrightApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .validateFieldExists("data")
    .parseJson()
    .getAsJsonObject("data")
    .get("id")
    .getAsString();
```

### 3. Request with Validation

**Old Approach (ApiUtils)**
```java
APIResponse response = ApiUtils.post(requestContext, "/users", payload, headers);
Assert.assertEquals(response.status(), 201);

JsonObject json = JsonParser.parseString(response.text()).getAsJsonObject();
Assert.assertTrue(json.get("success").getAsBoolean());
Assert.assertEquals(json.getAsJsonObject("data").get("firstName").getAsString(), "John");
```

**New Approach (PlaywrightApiClient)**
```java
PlaywrightApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .validateField("success", true)
    .validateField("data.firstName", "John");
```

## Feature Comparison Matrix

| Feature | ApiUtils | PlaywrightApiClient |
|---------|----------|---------------------|
| GET Request | ✓ | ✓ |
| POST Request | ✓ | ✓ |
| PUT Request | ✓ | ✓ |
| DELETE Request | ✓ | ✓ |
| PATCH Request | ✗ | ✓ |
| Form-Encoded POST | ✗ | ✓ |
| Fluent API | ✗ | ✓ |
| Status Assertions | Manual | Fluent `.expectStatus()` |
| Success Assertions | Manual | Fluent `.expectSuccess()` |
| Field Validation | Manual | Fluent `.validateField()` |
| JSON Parsing | Manual | Built-in `.parseJson()` |
| Header Builders | ✗ | ✓ |
| Token Auth Builders | ✗ | ✓ |
| Response Debugging | ✗ | ✓ |

## Migration Path

**Step 1:** Keep using `ApiUtils` for existing tests (no breaking changes)

**Step 2:** New tests can use `ApiClient` for enhanced features

**Step 3:** Gradually migrate existing tests to get fluent API benefits

## Code Examples by Use Case

### Use Case 1: Simple GET Request

```java
// Simple GET with default headers
ApiClient.get(requestContext, "/users")
    .expectSuccess();

// With custom headers
Map<String, String> headers = ApiClient.buildHeaders();
ApiClient.get(requestContext, "/users/1", headers)
    .expectStatus(200);
```

### Use Case 2: API Key Authentication

```java
Map<String, String> headers = ApiClient.buildHeadersWithApiKey("pk_practice_1234567890");

Map<String, Object> payload = new HashMap<>();
payload.put("email", "test@example.com");

ApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201);
```

### Use Case 3: Bearer Token Authentication

```java
// After OAuth2 login or token fetch
String token = AuthTokenManager.getValidToken(requestContext);

Map<String, String> headers = ApiClient.buildHeadersWithBearerToken(token);

ApiClient.get(requestContext, "/v1/auth/me", headers)
    .expectSuccess()
    .validateFieldExists("data.client");
```

### Use Case 4: OAuth2 Token Exchange

```java
Map<String, String> formData = new HashMap<>();
formData.put("grant_type", "client_credentials");
formData.put("client_id", "demo");
formData.put("client_secret", "password123");

ApiClient.postFormEncoded(requestContext, "/oauth/token", formData, null)
    .expectStatus(200)
    .validateFieldExists("access_token");
```

### Use Case 5: CRUD Workflow

```java
// CREATE
ApiResponse createResp = ApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201);
String id = createResp.parseJson().getAsJsonObject("data").get("id").getAsString();

// READ
ApiClient.get(requestContext, "/users/" + id, headers)
    .expectSuccess();

// UPDATE
ApiClient.put(requestContext, "/users/" + id, updatePayload, headers)
    .expectStatus(200);

// DELETE
ApiClient.delete(requestContext, "/users/" + id, headers)
    .expectSuccess();
```

### Use Case 6: Response Parsing

```java
ApiClient.get(requestContext, "/users", headers)
    .expectSuccess()
    .parseJson()
    .getAsJsonArray("data")
    .forEach(item -> {
        String email = item.getAsJsonObject().get("email").getAsString();
        LogUtils.info("User: " + email);
    });
```

### Use Case 7: Error Handling

```java
try {
    ApiClient.get(requestContext, "/users/invalid-id", headers)
        .expectSuccess();
} catch (AssertionError e) {
    LogUtils.info("Expected assertion failure: " + e.getMessage());
} catch (RuntimeException e) {
    LogUtils.info("Request execution error: " + e.getMessage());
}
```

## Integration Examples

### With TestNG

```java
public class MyApiTest {
    private APIRequestContext requestContext;
    
    @BeforeClass
    public void setup() {
        Playwright playwright = Playwright.create();
        requestContext = playwright.request().newContext(
            new APIRequest.NewContextOptions().setBaseURL("https://api.example.com")
        );
    }
    
    @Test
    public void testCreateUser() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        
        ApiClient.post(requestContext, "/users", payload, 
            ApiClient.buildHeadersWithApiKey("key"))
            .expectStatus(201)
            .validateFieldExists("data.id");
    }
    
    @AfterClass
    public void tearDown() {
        requestContext.dispose();
    }
}
```

### With Existing ApiUtils

Both utilities can be used in the same test:

```java
// Original approach for backward compatibility
APIResponse oldResponse = ApiUtils.get(requestContext, "/users/1", headers);

// New approach for new tests
ApiClient.get(requestContext, "/users/1", headers)
    .expectSuccess();
```

### With AuthTokenManager

```java
String token = AuthTokenManager.getValidToken(requestContext);
Map<String, String> headers = ApiClient.buildHeadersWithBearerToken(token);

ApiClient.get(requestContext, "/secured/endpoint", headers)
    .expectSuccess();
```

## Advanced Patterns

### Pattern 1: Fluent Chaining for Complex Validations

```java
ApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .validateFieldExists("data")
    .validateFieldExists("data.id")
    .validateField("success", true)
    .printResponse()
    .getRawResponse();
```

### Pattern 2: Extract Data and Make Follow-up Request

```java
String userId = ApiClient.post(requestContext, "/users", payload, headers)
    .expectStatus(201)
    .parseJson()
    .getAsJsonObject("data")
    .get("id")
    .getAsString();

ApiClient.get(requestContext, "/users/" + userId, headers)
    .expectSuccess();
```

### Pattern 3: Reusable Response Object

```java
ApiResponse response = ApiClient.get(requestContext, "/users/1", headers)
    .expectSuccess();

// Use response multiple times
String body1 = response.getBody();
JsonObject json = response.parseJson();
int status = response.getStatusCode();
```

### Pattern 4: Custom Header Building

```java
Map<String, String> headers = ApiClient.buildHeadersWithBearerToken(token);
headers = ApiClient.addHeader(headers, "X-Request-Id", UUID.randomUUID().toString());
headers = ApiClient.addHeader(headers, "X-Custom-Header", "value");

ApiClient.get(requestContext, "/endpoint", headers)
    .expectSuccess();
```

## Performance Considerations

- Both `ApiUtils` and `PlaywrightApiClient` use the same underlying Playwright APIRequestContext
- No performance difference between old and new approaches
- Fluent API benefits readability without performance cost
- Consider reusing APIRequestContext across multiple requests

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Cannot find symbol: headerValue" | Using old method that doesn't exist - use `.getBody()` instead |
| JSON parsing errors | Response is not JSON - use `.printResponse()` to debug |
| Status code mismatch | Use `.expectStatus(200)` instead of exact assertion |
| Headers not sent | Pass headers map explicitly to method |
| Null pointer in parseJson | Check response status first with `.expectSuccess()` |

## Files Modified/Created

1. **Created:** `src/main/java/utils/api/ApiClient.java` (NEW)
   - Generic HTTP methods (POST, GET, PUT, DELETE, PATCH)
   - Fluent API wrapper
   - Response helper methods
   - Header builders

2. **Created:** `src/test/java/day11/ApiClientDemoTest.java` (NEW)
   - Example usage of all ApiClient features
   - Demonstrates all HTTP methods
   - Shows fluent API usage

3. **Created:** `API_CLIENT_GUIDE.md` (NEW)
   - Comprehensive documentation
   - Usage examples
   - Best practices

4. **Unchanged:** `src/main/java/utils/ApiUtils.java`
   - Original utility still available
   - No breaking changes

## Testing the New Utility

```bash
# Run demo tests
mvn test -Dtest=ApiClientDemoTest

# Run all tests in day11
mvn test -Dtest=day11.*

# Run specific test
mvn test -Dtest=ApiClientDemoTest#testPostRequestWithFluentApi
```

## Next Steps

1. Review `API_CLIENT_GUIDE.md` for complete documentation
2. Run `ApiClientDemoTest` to see all features
3. Start using `PlaywrightApiClient` in new tests
4. Migrate existing tests gradually for cleaner code

