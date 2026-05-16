package day4;

import com.automation.playwright.pages.LoginPage;
import com.microsoft.playwright.*;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;


public class LoginTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private LoginPage loginPage;

    @BeforeClass
    public void beforeClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeMethod
    public void setUp(ITestContext testNGContext) {
        context = browser.newContext();
        page = context.newPage();

        // CRITICAL: Share the page instance with the TestNG context for the listener to use
        testNGContext.setAttribute("PlaywrightPage", page);

        loginPage = new LoginPage(page);
        loginPage.navigate();
    }

    @Test(priority = 1, description = "TC1 - Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        loginPage.login("student", "Password123");
        String actualHeader = loginPage.getSuccessMessage();
        Assert.assertEquals(actualHeader, "Logged In Successfully");
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredsProvider() {
        return new Object[][]{
                {"TC2 - Invalid Username", "invalidUser", "Password123", "Your username is invalid!"},
                {"TC3 - Invalid Password", "student", "wrongPassword", "Your password is invalid!"}
        };
    }

    @Test(priority = 2, dataProvider = "invalidCredentials", description = "Verify Error Messages")
    public void testInvalidLogin(String tcDesc, String username, String password, String expectedError) {
        loginPage.login(username, password);
        String actualError = loginPage.getErrorMessage();
        Assert.assertTrue(actualError.contains(expectedError));
    }

    @AfterMethod
    public void tearDown() {
        if (context != null) context.close();
    }

    @AfterClass
    public void afterClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}