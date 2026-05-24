package utils;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import utils.reports.LogUtils;
import java.util.HashMap;
import java.util.Map;

public class ApiUtils {

    /**
     * Helper method to initialize a clean, standalone APIRequestContext with default fallback headers.
     * @param baseUrl The target base URL of your API service backend
     */
    public static APIRequestContext createRequestContext(Playwright playwright, String baseUrl) {
        LogUtils.info("Initializing API Request Context wrapper for domain: " + baseUrl);

        // Define clean, global request headers to pass common security/proxy gateway checkpoints
        Map<String, String> globalHeaders = new HashMap<>();
        globalHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        globalHeaders.put("Accept", "application/json, text/plain, */*");
        globalHeaders.put("Content-Type", "application/json");

        return playwright.request().newContext(
                new APIRequest.NewContextOptions()
                        .setBaseURL(baseUrl)
                        .setExtraHTTPHeaders(globalHeaders) // Injects valid headers globally across all requests
        );
    }

    /**
     * Generic GET Request
     */
    public static APIResponse get(APIRequestContext request, String endpoint, Map<String, String> headers) {
        LogUtils.info("Sending HTTP GET Request to endpoint: " + endpoint);
        RequestOptions options = RequestOptions.create();
        if (headers != null) {
            headers.forEach(options::setHeader);
        }
        return request.get(endpoint, options);
    }

    /**
     * Generic POST Request with Payload
     */
    public static APIResponse post(APIRequestContext request, String endpoint, Map<String, Object> payload, Map<String, String> headers) {
        LogUtils.info("Sending HTTP POST Request to endpoint: " + endpoint);
        RequestOptions options = RequestOptions.create();
        if (headers != null) {
            headers.forEach(options::setHeader);
        }
        if (payload != null) {
            options.setData(payload);
        }
        return request.post(endpoint, options);
    }

    /**
     * Generic PUT (Update) Request with Payload
     */
    public static APIResponse put(APIRequestContext request, String endpoint, Map<String, Object> payload, Map<String, String> headers) {
        LogUtils.info("Sending HTTP PUT Request to endpoint: " + endpoint);
        RequestOptions options = RequestOptions.create();
        if (headers != null) {
            headers.forEach(options::setHeader);
        }
        if (payload != null) {
            options.setData(payload);
        }
        return request.put(endpoint, options);
    }

    /**
     * Generic DELETE Request
     */
    public static APIResponse delete(APIRequestContext request, String endpoint, Map<String, String> headers) {
        LogUtils.info("Sending HTTP DELETE Request to endpoint: " + endpoint);
        RequestOptions options = RequestOptions.create();
        if (headers != null) {
            headers.forEach(options::setHeader);
        }
        return request.delete(endpoint, options);
    }
}