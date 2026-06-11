package temp;

import com.microsoft.playwright.*;

public class FallbackSelectorsDemo {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");

            // CSS Fallback: Standard class-based selector
            // Playwright auto-waits for this button to be attached to the DOM
            Locator addButton = page.locator(".example button");
            addButton.click();

            // XPath Fallback: Useful for complex parent-child relationships
            // This finds the 'Delete' button only if it is inside the elements div
            Locator deleteBtn = page.locator("//div[@id='elements']//button[text()='Delete']");

            // Core Action: click() will automatically wait for the element to be visible
            if (deleteBtn.isVisible()) {
                deleteBtn.click();
                System.out.println("Element added and removed successfully.");
            }

            browser.close();
        }
    }
}