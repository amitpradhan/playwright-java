package utils.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import utils.reports.LogUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic REST API Client Utility
 * Provides a comprehensive wrapper around Playwright's APIRequestContext with:
 * - Generic HTTP methods (POST, GET, PUT, DELETE, PATCH)
 * - Fluent API design for building requests
 * - Response parsing and validation helpers
 * - Common assertion utilities
 * - Better error handling and logging
 *
 * Usage Examples:
 * - POST: ApiClient.post(requestContext, "/users", payload, headers).expectStatus(201)
 * - GET: ApiClient.get(requestContext, "/users/1", headers).expectStatus(200).parseJson()
 * - PUT: ApiClient.put(requestContext, "/users/1", payload, headers).validateResponse()
 * - DELETE: ApiClient.delete(requestContext, "/users/1", headers).expectStatus(204)
 */
public class ApiClient {

    /**
     * Fluent API Response wrapper for chainable assertions and parsing
     */
    public static class ApiResponse {
        private final APIResponse rawResponse;
        private final String endpoint;
        private final String method;
        private JsonObject parsedJson;

        public ApiResponse(APIResponse response, String endpoint, String method) {
            this.rawResponse = response;
            this.endpoint = endpoint;
            this.method = method;
        }

        /**
         * Get the raw HTTP status code
         */
        public int getStatusCode() {
            return rawResponse.status();
        }

        /**
         * Get the raw response body as string
         */
        public String getBody() {
            return rawResponse.text();
        }

        // Note: Playwright APIResponse doesn't expose header access in the current version

        /**
         * Parse response body as JSON object
         */
        public JsonObject parseJson() {
            if (parsedJson == null) {
                try {
                    String responseBody = getBody();
                    parsedJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    LogUtils.debug("Response parsed successfully as JSON from: " + endpoint);
                } catch (Exception e) {
                    LogUtils.error("Failed to parse response as JSON", e);
                    throw new RuntimeException("Response parsing failed for endpoint: " + endpoint, e);
                }
            }
            return parsedJson;
        }

        /**
         * Parse response body as JSON element (for arrays or mixed types)
         */
        public JsonElement parseJsonElement() {
            try {
                String responseBody = getBody();
                return JsonParser.parseString(responseBody);
            } catch (Exception e) {
                LogUtils.error("Failed to parse response as JSON element", e);
                throw new RuntimeException("Response parsing failed for endpoint: " + endpoint, e);
            }
        }

        /**
         * Fluent assertion: Expect specific status code
         */
        public ApiResponse expectStatus(int expectedStatus) {
            int actualStatus = getStatusCode();
            if (actualStatus != expectedStatus) {
                String errorMsg = String.format(
                        "Status code mismatch! Expected: %d, Got: %d for %s %s. Response: %s",
                        expectedStatus, actualStatus, method, endpoint, getBody()
                );
                LogUtils.error(errorMsg, new Exception(errorMsg));
                throw new AssertionError(errorMsg);
            }
            LogUtils.info("✓ Status code validation passed: " + expectedStatus + " for " + method + " " + endpoint);
            return this;
        }

        /**
         * Fluent assertion: Expect successful status (200-299 range)
         */
        public ApiResponse expectSuccess() {
            int status = getStatusCode();
            if (status < 200 || status >= 300) {
                String errorMsg = String.format(
                        "Expected success status (200-299), but got: %d for %s %s. Response: %s",
                        status, method, endpoint, getBody()
                );
                LogUtils.error(errorMsg, new Exception(errorMsg));
                throw new AssertionError(errorMsg);
            }
            LogUtils.info("✓ Success status validation passed for " + method + " " + endpoint);
            return this;
        }

        /**
         * Validate response body contains JSON field with expected value
         */
        public ApiResponse validateField(String fieldPath, Object expectedValue) {
            try {
                JsonObject json = parseJson();
                JsonElement element = json.get(fieldPath);
                
                if (element == null) {
                    String errorMsg = "Expected field '" + fieldPath + "' not found in response";
                    LogUtils.error(errorMsg, new Exception(errorMsg));
                    throw new AssertionError(errorMsg);
                }

                Object actualValue = element.isJsonPrimitive() ? 
                        element.getAsJsonPrimitive().getAsString() : element.toString();

                if (!actualValue.equals(String.valueOf(expectedValue))) {
                    String errorMsg = "Field value mismatch! Field: " + fieldPath + 
                            ", Expected: " + expectedValue + ", Got: " + actualValue;
                    LogUtils.error(errorMsg, new Exception(errorMsg));
                    throw new AssertionError(errorMsg);
                }

                LogUtils.info("✓ Field validation passed: " + fieldPath + " = " + expectedValue);
                return this;
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                LogUtils.error("Field validation error", e);
                throw new RuntimeException("Field validation failed", e);
            }
        }

        /**
         * Validate response contains specific field
         */
        public ApiResponse validateFieldExists(String fieldPath) {
            try {
                JsonObject json = parseJson();
                JsonElement element = json.get(fieldPath);
                
                if (element == null) {
                    String errorMsg = "Expected field '" + fieldPath + "' not found in response";
                    LogUtils.error(errorMsg, new Exception(errorMsg));
                    throw new AssertionError(errorMsg);
                }

                LogUtils.info("✓ Field existence validation passed: " + fieldPath);
                return this;
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                LogUtils.error("Field existence validation error", e);
                throw new RuntimeException("Field validation failed", e);
            }
        }

        /**
         * Print full response for debugging
         */
        public ApiResponse printResponse() {
            LogUtils.info("===== " + method + " " + endpoint + " =====");
            LogUtils.info("Status Code: " + getStatusCode());
            LogUtils.info("Response Body:\n" + getBody());
            return this;
        }

        /**
         * Get raw API response object
         */
        public APIResponse getRawResponse() {
            return rawResponse;
        }
    }

    /**
     * Execute GET request
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse get(APIRequestContext requestContext, String endpoint, Map<String, String> headers) {
        LogUtils.info("Executing: GET " + endpoint);
        
        try {
            RequestOptions options = RequestOptions.create();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }
            
            APIResponse response = requestContext.get(endpoint, options);
            LogUtils.debug("GET request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "GET");
        } catch (Exception e) {
            LogUtils.error("GET request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("GET request failed", e);
        }
    }

    /**
     * Execute GET request with no headers
     */
    public static ApiResponse get(APIRequestContext requestContext, String endpoint) {
        return get(requestContext, endpoint, null);
    }

    /**
     * Execute POST request with JSON payload
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param payload Request payload as Map (will be converted to JSON)
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse post(APIRequestContext requestContext, String endpoint, 
                                   Map<String, Object> payload, Map<String, String> headers) {
        LogUtils.info("Executing: POST " + endpoint);
        
        try {
            RequestOptions options = RequestOptions.create();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }
            if (payload != null && !payload.isEmpty()) {
                options.setData(payload);
            }
            
            APIResponse response = requestContext.post(endpoint, options);
            LogUtils.debug("POST request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "POST");
        } catch (Exception e) {
            LogUtils.error("POST request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("POST request failed", e);
        }
    }

    /**
     * Execute POST request with JSON payload and no custom headers
     */
    public static ApiResponse post(APIRequestContext requestContext, String endpoint, Map<String, Object> payload) {
        return post(requestContext, endpoint, payload, null);
    }

    /**
     * Execute POST request with form-encoded payload
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param formData Form data to send (will be URL-encoded)
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse postFormEncoded(APIRequestContext requestContext, String endpoint,
                                              Map<String, String> formData, Map<String, String> headers) {
        LogUtils.info("Executing: POST " + endpoint + " (form-encoded)");
        
        try {
            // Build form-encoded body
            StringBuilder formBody = new StringBuilder();
            if (formData != null && !formData.isEmpty()) {
                int count = 0;
                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    if (count > 0) formBody.append("&");
                    formBody.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
                    count++;
                }
            }

            RequestOptions options = RequestOptions.create()
                    .setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }
            
            options.setData(formBody.toString());

            APIResponse response = requestContext.post(endpoint, options);
            LogUtils.debug("POST form-encoded request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "POST");
        } catch (Exception e) {
            LogUtils.error("POST form-encoded request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("POST form-encoded request failed", e);
        }
    }

    /**
     * Execute PUT request with JSON payload
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param payload Request payload as Map (will be converted to JSON)
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse put(APIRequestContext requestContext, String endpoint,
                                  Map<String, Object> payload, Map<String, String> headers) {
        LogUtils.info("Executing: PUT " + endpoint);
        
        try {
            RequestOptions options = RequestOptions.create();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }
            if (payload != null && !payload.isEmpty()) {
                options.setData(payload);
            }

            APIResponse response = requestContext.put(endpoint, options);
            LogUtils.debug("PUT request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "PUT");
        } catch (Exception e) {
            LogUtils.error("PUT request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("PUT request failed", e);
        }
    }

    /**
     * Execute PUT request with JSON payload and no custom headers
     */
    public static ApiResponse put(APIRequestContext requestContext, String endpoint, Map<String, Object> payload) {
        return put(requestContext, endpoint, payload, null);
    }

    /**
     * Execute PATCH request with JSON payload
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param payload Request payload as Map (will be converted to JSON)
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse patch(APIRequestContext requestContext, String endpoint,
                                    Map<String, Object> payload, Map<String, String> headers) {
        LogUtils.info("Executing: PATCH " + endpoint);
        
        try {
            RequestOptions options = RequestOptions.create();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }
            if (payload != null && !payload.isEmpty()) {
                options.setData(payload);
            }

            APIResponse response = requestContext.patch(endpoint, options);
            LogUtils.debug("PATCH request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "PATCH");
        } catch (Exception e) {
            LogUtils.error("PATCH request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("PATCH request failed", e);
        }
    }

    /**
     * Execute PATCH request with JSON payload and no custom headers
     */
    public static ApiResponse patch(APIRequestContext requestContext, String endpoint, Map<String, Object> payload) {
        return patch(requestContext, endpoint, payload, null);
    }

    /**
     * Execute DELETE request
     *
     * @param requestContext APIRequestContext for HTTP requests
     * @param endpoint API endpoint path
     * @param headers Custom headers (null for default headers)
     * @return ApiResponse wrapper for fluent assertions
     */
    public static ApiResponse delete(APIRequestContext requestContext, String endpoint, Map<String, String> headers) {
        LogUtils.info("Executing: DELETE " + endpoint);
        
        try {
            RequestOptions options = RequestOptions.create();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(options::setHeader);
            }

            APIResponse response = requestContext.delete(endpoint, options);
            LogUtils.debug("DELETE request completed. Status: " + response.status());
            return new ApiResponse(response, endpoint, "DELETE");
        } catch (Exception e) {
            LogUtils.error("DELETE request failed for endpoint: " + endpoint, e);
            throw new RuntimeException("DELETE request failed", e);
        }
    }

    /**
     * Execute DELETE request with no custom headers
     */
    public static ApiResponse delete(APIRequestContext requestContext, String endpoint) {
        return delete(requestContext, endpoint, null);
    }

    /**
     * Build headers map with common defaults
     */
    public static Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        return headers;
    }

    /**
     * Build headers with API key authentication
     */
    public static Map<String, String> buildHeadersWithApiKey(String apiKey) {
        Map<String, String> headers = buildHeaders();
        headers.put("X-API-Key", apiKey);
        return headers;
    }

    /**
     * Build headers with Bearer token authentication
     */
    public static Map<String, String> buildHeadersWithBearerToken(String token) {
        Map<String, String> headers = buildHeaders();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    /**
     * Build headers with custom authorization header
     */
    public static Map<String, String> buildHeadersWithAuth(String authScheme, String authValue) {
        Map<String, String> headers = buildHeaders();
        headers.put("Authorization", authScheme + " " + authValue);
        return headers;
    }

    /**
     * Add custom header to existing headers map
     */
    public static Map<String, String> addHeader(Map<String, String> headers, String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return headers;
    }

    /**
     * URL encode a string
     */
    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            LogUtils.error("URL encoding failed", e);
            return value;
        }
    }
}

