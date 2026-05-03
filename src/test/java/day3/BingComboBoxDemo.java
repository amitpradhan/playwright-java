package day3;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class BingComboBoxDemo {
    public static void main(String[] args) {
        try(Playwright playwright = Playwright.create()){
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
//            page.navigate("https://www.bing.com");
//            page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Enter your search here - Search suggestions will show as you type"))
//                    .fill("Playwright Java Automation");
//            page.getByLabel("playwright java automation").click();
////            page.getByText("playwright java automation").click();


            page.navigate("https://www.bing.com");
            Locator searchBar = page.getByRole(AriaRole.COMBOBOX); // Simplified locator

            searchBar.click();
            searchBar.pressSequentially("Playwright Java Automation", new Locator.PressSequentiallyOptions().setDelay(100));

            // Wait for and click the suggestion
            page.locator("li span:has-text('playwright java automation')").first().click();
            page.waitForTimeout(2000);
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Playwright Java Tutorial 2026: A Complete Guide")).click();

    }
}}
