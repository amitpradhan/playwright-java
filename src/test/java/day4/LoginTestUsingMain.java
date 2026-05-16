package day4;

import com.automation.playwright.pages.LoginPage;
import com.microsoft.playwright.*;

public class LoginTestUsingMain {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            // Launch Browser (headless = false lets you see the action)
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Initialize Page Object
            LoginPage loginPage = new LoginPage(page);

            // Execute Login steps
            loginPage.navigate();

            // Using credentials provided on the practice site: student / Password123
            loginPage.login("student", "Password123");

            // Verify Result
            String header = loginPage.getStatusMessage();
            if (header.contains("Logged In Successfully")) {
                System.out.println("Test Passed: Login Successful!");
            } else {
                System.out.println("Test Failed: Could not find success message.");
            }

            // Clean up
            browser.close();
        }
    }
}
