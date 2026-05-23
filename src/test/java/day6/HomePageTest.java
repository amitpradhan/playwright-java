package day6;

import com.automation.playwright.pages.HomePage;
import com.automation.playwright.pages.LoginPage;
import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import utils.reports.LogUtils;


public class HomePageTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private LoginPage loginPage;
    private HomePage homePage;

    @BeforeClass
    public void beforeClass() {
        LogUtils.info("Starting HomePage Test Run Suite execution container.");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeMethod
    public void setUp(ITestContext testNGContext) {
        context = browser.newContext();
        page = context.newPage();

        // Pass the active viewport instance over to the Extent Screenshot Listener
        testNGContext.setAttribute("PlaywrightPage", page);

        loginPage = new LoginPage(page);
        homePage = new HomePage(page);

        // Prerequisite Setup: Log in securely to unlock the dashboard components
        loginPage.navigate();
        loginPage.login("student", "Password123");
    }

    @Test(priority = 1, description = "TC4 - Verify Logout component availability on dashboard landing page")
    public void testLogoutButtonPresence() {
        LogUtils.info("Executing visibility tracking metrics for the Logout handle.");
        Assert.assertTrue(homePage.isLogoutButtonDisplayed(), "Logout button was not found on the post-login dashboard!");
    }

    @DataProvider(name = "primaryNavigationMenus")
    public Object[][] getNavigationMenus() {
        return new Object[][]{
                {"Home"},
                {"Practice"},
                {"Courses"},
                {"Blog"},
                {"Contact"}
        };
    }

    @Test(priority = 2, dataProvider = "primaryNavigationMenus", description = "TC5 - Validate global navigation menus and link connectivity")
    public void testMenuAvailabilityAndNavigation(String menuName) {
        LogUtils.info("Verifying existence and navigation flow for menu: " + menuName);

        // 1. Assert the link option is clearly available
        Assert.assertTrue(homePage.isMenuDisplayed(menuName), "Primary menu link '" + menuName + "' is missing!");

        // 2. Perform interactive test click action
        homePage.clickMenu(menuName);

        // Brief check to confirm page interaction didn't break down into a completely empty tab blank space
        Assert.assertFalse(page.url().isEmpty(), "Page navigation link left url empty state after clicking " + menuName);
    }

    @AfterMethod
    public void tearDown() {
        LogUtils.info("Closing active scenario browser context tabs safely.");
        if (context != null) context.close();
    }

    @AfterClass
    public void afterClass() {
        LogUtils.info("Terminating underlying Playwright browser communication bindings.");
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}