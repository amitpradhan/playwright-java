package utils.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.Page;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.util.Base64;

public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    /**
     * Exposes the current thread-safe ExtentTest instance.
     * This allows LogUtils to route live Log4j steps directly into the report.
     */
    public static ExtentTest getTest() {
        return test.get();
    }

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("target/ExtentReport.html");
        sparkReporter.config().setReportName("Playwright Automation Results");
        sparkReporter.config().setDocumentTitle("Test Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Framework", "Playwright-Java");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = "";
        if (result.getMethod().getDescription() != null && !result.getMethod().getDescription().trim().isEmpty()) {
            testName = result.getMethod().getDescription();
        } else {
            testName = result.getMethod().getMethodName();
        }

        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            testName = testName + " - [" + parameters[0].toString() + "]";
        }

        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
    }


    @Override
    public void onTestSuccess(ITestResult result) {
        String base64Screenshot = captureScreenshot(result);

        // Guard check: Only build media if a browser screenshot string actually exists
        if (base64Screenshot != null && !base64Screenshot.isEmpty()) {
            test.get().pass("<b>Test Passed Successfully</b>",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
        } else {
            test.get().pass("<b>Test Passed Successfully</b> (No screenshot captured for non-UI test)");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String base64Screenshot = captureScreenshot(result);
        String errorMessage = "<b>Test Failed: </b>" + result.getThrowable();

        // Guard check: Prevent ExtentReports from crashing on pure API or background task failures
        if (base64Screenshot != null && !base64Screenshot.isEmpty()) {
            test.get().fail(errorMessage,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
        } else {
            test.get().fail(errorMessage + "<br><span style='color:red;'>[Note: No UI viewport active to capture screenshots]</span>");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Test Skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    // Helper method to extract the page context and snap the image
    private String captureScreenshot(ITestResult result) {
        try {
            // Retrieve the shared Playwright Page instance from the TestNG Context
            ITestContext context = result.getTestContext();
            Page page = (Page) context.getAttribute("PlaywrightPage");

            if (page != null) {
                // Take screenshot and convert bytes to Base64 String
                byte[] buffer = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
                return Base64.getEncoder().encodeToString(buffer);
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
        return "";
    }
}