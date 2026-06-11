package temp;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

public class TypingActionDemo {
    public static void main(String[] args) {
//

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate("https://www.bing.com/?toWww=1&redig=163330BE46894A8DAC85496EF9435A74");
            page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Enter your search here -")).click();
            page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Enter your search here -")).fill("test");
            page.getByText("speed").click();
            Page page1 = page.waitForPopup(() -> {
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Speedtest Create an Account")).click();
            });
        }
}


}