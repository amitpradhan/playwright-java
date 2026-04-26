package day1;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;

public class TestSetup {

    public static void main(String[] args) {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.webkit().launch();
                Page page = browser.newPage();
                page.navigate("https://playwright.dev/");
                System.out.println("Test");
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
//
    }
}}
