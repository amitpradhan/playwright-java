package day6;

import com.automation.playwright.pages.HomePage;
import com.automation.playwright.pages.LoginPage;
import com.automation.playwright.pages.PracticePage;
import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import utils.reports.LogUtils;

import java.util.List;

public class PracticePageTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private LoginPage loginPage;
    private HomePage homePage;
    private PracticePage practicePage;

    @BeforeClass
    public void beforeClass() {
        LogUtils.info("Initializing PracticePage Verification Test Suite Environment.");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeMethod
    public void setUp(ITestContext testNGContext) {
        context = browser.newContext();
        page = context.newPage();
        testNGContext.setAttribute("PlaywrightPage", page);

        loginPage = new LoginPage(page);
        homePage = new HomePage(page);
        practicePage = new PracticePage(page);

        // Step 1: Securely Log in to the application
        loginPage.navigate();
        loginPage.login("student", "Password123");

        // Step 2: Navigate away from the landing page using the primary "Practice" link menu
        homePage.clickMenu("Practice");
    }

    @Test(priority = 1, description = "TC6 - Calculate link density metrics inside Practice workspace")
    public void testLinkCountInPracticePage() {
        int linkCount = practicePage.getNumberOfLinks();
        LogUtils.info("Test verified: Total active links inside workspace area = " + linkCount);

        // Based on the HTML snippet provided, there are exactly 3 links (Test Login, Exceptions, Table)
        Assert.assertEquals(linkCount, 3, "The verified layout count did not match structural DOM specs!");
    }

    @Test(priority = 2, description = "TC7 - Inspect page connections to confirm there are no broken links")
    public void testForBrokenLinksInPracticePage() {
        List<String> brokenLinksDiscovered = practicePage.findBrokenLinks();

        LogUtils.info("Link evaluation analysis complete.");
        if (!brokenLinksDiscovered.isEmpty()) {
            LogUtils.warn("Test Failed: Discovered broken links in execution scope: " + brokenLinksDiscovered);
        } else {
            LogUtils.info("Success! Zero dead or broken links discovered on this workspace page viewport.");
        }

        // Assertion fails if the collection contains any broken links
        Assert.assertTrue(brokenLinksDiscovered.isEmpty(),
                "Broken URLs were detected inside the main container workspace area! Check log artifacts.");
    }

    @AfterMethod
    public void tearDown() {
        LogUtils.info("Closing runtime context views safely.");
        if (context != null) context.close();
    }

    @AfterClass
    public void afterClass() {
        LogUtils.info("Killing open parent Playwright system communication sessions.");
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}