package temp;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class TypingActionDemo2 {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            page.navigate("https://www.bing.com");

            // LOCATOR: getByRole for the search box
            Locator searchBox = page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Enter your search here - Search suggestions will show as you type"));

            // ACTION: fill() - Blasts the text in instantly (Best for performance)
            searchBox.fill("Playwright Framework");
            page.waitForTimeout(15000);
            searchBox.clear(); // Empty the field

            // ACTION: type() - Simulates a human typing with a delay (Best for triggering search suggestions)
            searchBox.type("Automation with Java", new Locator.TypeOptions().setDelay(150));

            // Pressing Enter key
            page.keyboard().press("Enter");

            // Verification using getByText
            // Playwright will wait up to 30 seconds (default) for this text to appear
//            String resultsText = page.getByText("results found").innerText();

            page.locator("#b_results").waitFor();

            String resultsText = page.locator("#b_tween").innerText();
            System.out.println("Search completed: " + resultsText);
            System.out.println("Search completed: " + resultsText);

            browser.close();

        }
    }
}