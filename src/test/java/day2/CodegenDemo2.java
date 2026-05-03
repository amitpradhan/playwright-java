package day2;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import java.util.*;

public class CodegenDemo2 {

    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.locator("body").click();
            page.navigate("https://timesofindia.indiatimes.com/");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("India").setExact(true)).click();
            page.locator(".Wi1aZ").click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("TOI India / May 2, 2026 Repolling in 15 West Bengal booths after EVM tampering")).click();
            assertThat(page.locator("h1")).matchesAriaSnapshot("- heading /Repolling in \\d+ West Bengal booths after EVM tampering claims - full list/ [level=1]");
        }
    }
}
