package day7;

import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import utils.LinkUtils;
import utils.reports.LogUtils;

import java.util.List;

public class TimesOfIndiaTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeClass
    public void beforeClass() {
        LogUtils.info("Initializing Times of India Link Performance Test Container.");
        playwright = Playwright.create();

        // Launching with a relaxed timeout setup to accommodate heavy asset loads on news portals
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeMethod
    public void setUp(ITestContext testNGContext) {
        context = browser.newContext();
        page = context.newPage();
        testNGContext.setAttribute("PlaywrightPage", page);

        // Set a global navigation limit of 60 seconds because media sites load heavy tracking trackers
        page.setDefaultNavigationTimeout(60000);

        LogUtils.info("Navigating to Times of India Homepage...");
        page.navigate("https://timesofindia.indiatimes.com/");
    }

    @Test(priority = 1, description = "TC8 - Execute full regression health sweep for all homepage links")
    public void testTimesOfIndiaBrokenLinks() {
        // Step 1: Extract every active, unique link out of the root document body
        List<String> allHomepageLinks = LinkUtils.getAllLinksOnPage(page, "body");

        LogUtils.info("Extracted " + allHomepageLinks.size() + " distinct URLs from home layout view.");

        // Step 2: Probe all collected links concurrently via our parallel network context engine
        List<String> brokenLinksReport = LinkUtils.checkBrokenLinksParallel(page, allHomepageLinks);

        LogUtils.info("Summary Report: Discovered total of " + brokenLinksReport.size() + " failures.");

        // If broken links are found, print them out line-by-line for your team to debug
        if (!brokenLinksReport.isEmpty()) {
            System.err.println("--- LIST OF DETECTED BROKEN ENDPOINTS ---");
            brokenLinksReport.forEach(System.err::println);
        }

        // soft assertion fallback recommendation: Production news environments change minute-by-minute.
        // If you want your CI run to remain green even if a 3rd-party ad link is broken, use standard console warnings,
        // or assert against a threshold (e.g. less than 5% links are broken).
        Assert.assertTrue(brokenLinksReport.size() < (allHomepageLinks.size() * 0.05),
                "More than 5% of homepage endpoints failed health verification loops!");
    }

    @AfterMethod
    public void tearDown() {
        LogUtils.info("Terminating script scenario tab execution view context.");
        if (context != null) context.close();
    }

    @AfterClass
    public void afterClass() {
        LogUtils.info("Disconnecting framework automation drivers.");
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}