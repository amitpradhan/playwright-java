package utils;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.reports.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class LinkUtils {

    /**
     * Generic method to find all valid href links within a specified page or selector container.
     */
    public static List<String> getAllLinksOnPage(Page page, String containerSelector) {
        LogUtils.trace("Entering getAllLinksOnPage for container: " + containerSelector);
        List<String> hrefList = new ArrayList<>();

        // Locate all anchor tags inside the target section/container
        Locator links = page.locator(containerSelector + " a");
        int totalLinks = links.count();
        LogUtils.info("Found total raw anchor links in DOM: " + totalLinks);

        for (int i = 0; i < totalLinks; i++) {
            String href = links.nth(i).getAttribute("href");
            // Filter out empty links, javascript links, or page anchors (#)
            if (href != null && !href.trim().isEmpty() && !href.startsWith("#") && !href.startsWith("javascript:")) {
                hrefList.add(href.trim());
            }
        }

        LogUtils.info("Filtered down to " + hrefList.size() + " active URLs for verification.");
        return hrefList;
    }

    /**
     * Reusable method that hits URLs concurrently using Playwright API engine to track broken links.
     * Returns a list of all broken URLs discovered.
     */
    public static List<String> checkBrokenLinks(Page page, List<String> urls) {
        LogUtils.trace("Starting broken link health validation scans.");
        List<String> brokenLinks = new ArrayList<>();

        // Create an isolated request context for fast HTTP probing
        APIRequestContext requestContext = page.context().request();

        for (String url : urls) {
            try {
                // Use a standard HEAD or GET request to fetch response codes without downloading full page weight
                APIResponse response = requestContext.get(url);
                int statusCode = response.status();

                if (statusCode >= 400) {
                    LogUtils.warn("Broken Link Discovered! URL: " + url + " returned Status Code: " + statusCode);
                    brokenLinks.add(url);
                } else {
                    LogUtils.debug("Link Health OK -> Status: " + statusCode + " for URL: " + url);
                }
            } catch (Exception e) {
                LogUtils.error("Network Connectivity Failure encountered probing target URL: " + url, e);
                brokenLinks.add(url); // Count timeouts or completely dropped targets as broken
            }
        }

        return brokenLinks;
    }
}