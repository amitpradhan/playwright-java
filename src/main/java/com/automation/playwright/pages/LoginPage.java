package com.automation.playwright.pages;

import com.microsoft.playwright.Page;
import utils.reports.LogUtils;


public class LoginPage {
    private final Page page;
    private final String usernameInput = "#username";
    private final String passwordInput = "#password";
    private final String submitButton = "#submit";
    private final String successHeader = "h1.post-title";
    private final String errorMessage = "#error";

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        LogUtils.trace("Entering navigate() method execution.");
        LogUtils.info("Navigating to Practice Test Login Page...");

        try {
            page.navigate("https://practicetestautomation.com/practice-test-login/");
            LogUtils.debug("Navigation complete. Current URL target loaded successfully.");
        } catch (Exception e) {
            LogUtils.error("Failed to complete initial page navigation!", e);
            throw e;
        }
        LogUtils.trace("Exiting navigate() method sequence.");
    }

    public void login(String username, String password) {
        LogUtils.trace("Entering login() method structure execution.");
        LogUtils.debug("Runtime Arguments Extracted - User: " + username + ", Pass: [PROTECTED]");

        LogUtils.info("Filling login form fields via Playwright locator context.");
        page.fill(usernameInput, username);
        page.fill(passwordInput, password);

        LogUtils.info("Clicking the submission action button.");
        page.click(submitButton);
        LogUtils.trace("Exiting login() method sequence.");
    }

    public String getSuccessMessage() {
        LogUtils.trace("Entering getSuccessMessage() frame execution.");
        LogUtils.info("Scraping clean validation text header from UI.");
        return page.textContent(successHeader).trim();
    }

    public String getErrorMessage() {
        LogUtils.trace("Entering getErrorMessage() sequence.");
        LogUtils.warn("Login rejection state triggered. Waiting for error banner element.");

        try {
            page.waitForSelector(errorMessage);
            String errorText = page.textContent(errorMessage).trim();
            LogUtils.debug("Error message captured directly from DOM: " + errorText);
            return errorText;
        } catch (Exception e) {
            LogUtils.error("An explicit failure occurred while trying to extract the UI error banner text!", e);
            throw e;
        } finally {
            LogUtils.trace("Exiting getErrorMessage() sequence.");
        }
    }
}