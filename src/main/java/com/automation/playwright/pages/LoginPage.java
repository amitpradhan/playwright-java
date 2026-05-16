package com.automation.playwright.pages;
import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    // 1. Locators (using IDs and CSS for reliability)
    private final String usernameInput = "#username";
    private final String passwordInput = "#password";
    private final String submitButton = "#submit";
    private final String successMessage = "h1.post-title";
    private final String successHeader = "h1.post-title";
    private final String errorMessage = "#error";

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("https://practicetestautomation.com/practice-test-login/");
    }

    // 2. The Login Method
    public void login(String username, String password) {
        page.fill(usernameInput, username);
        page.fill(passwordInput, password);
        page.click(submitButton);
    }

    public String getStatusMessage() {
        // Returns the heading if login is successful
        return page.textContent(successMessage);
    }

    public String getSuccessMessage() {
        return page.textContent(successHeader).trim();
    }

    public String getErrorMessage() {
        // Wait for the error element to be visible before grabbing text
        page.waitForSelector(errorMessage);
        return page.textContent(errorMessage).trim();
    }
}
