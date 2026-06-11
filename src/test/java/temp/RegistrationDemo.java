package temp;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class RegistrationDemo {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            page.navigate("https://demo.automationtesting.in/Register.html");

            // getByPlaceholder: Great for modern inputs without explicit labels
            page.getByPlaceholder("First Name").fill("John");
            page.getByPlaceholder("Last Name").fill("Doe");

            // getByLabel: Targets the input associated with the <label> text
//            page.getByLabel("Address").fill("123 Playwright Lane, Java City");
//            page.getByPlaceholder("Address").fill("123 Playwright Lane");
            page.locator("textarea[ng-model='Adress']")
                    .fill("123 Playwright Lane, Java City");

            // getByRole + Exact Match: Ensures we click the specific radio button
//            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Male")).check();
            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Male").setExact(true)).check();

            // getByRole for Checkboxes
//            page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Movies").setExact(true)).check();

            // We use getByLabel because on that site, the text 'Movies' is usually inside a label tag
            page.getByLabel("Movies").check();

            // OR if you want to stick to Role, make it case-insensitive and non-exact
//            page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("movies").setExact(false)).check();

            // Handling Dropdowns with selectOption
            page.locator("#Skills").selectOption("Java");

            System.out.println("Form filled using user-centric locators!");
            browser.close();
        }
    }
}