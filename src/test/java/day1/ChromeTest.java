package day1;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ChromeTest {
    public static void main(String[] args) {

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false));

//            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().
//                    setExecutablePath(Paths.get("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"))
//                    .setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://playwright.dev/");
            System.out.println(browser.browserType().name()+ "-->" + browser.version());
            System.out.println("end");
        }
    }
}
