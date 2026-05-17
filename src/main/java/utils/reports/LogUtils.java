package utils.reports;

import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {
    private static final Logger log = LogManager.getLogger(LogUtils.class);

    // Logs to Log4j (Console/File) and routes to Extent Report if available
    public static void trace(String message) {
        log.trace(message);
        ExtentTest currentTest = ExtentReportListener.getTest();
        if (currentTest != null) {
            // Extent reports don't have a built-in 'trace' label style, so we map it to a subtle info log
            currentTest.info("<span style='color:gray; font-size:11px;'>[TRACE] " + message + "</span>");
        }
    }

    public static void debug(String message) {
        log.debug(message);
        ExtentTest currentTest = ExtentReportListener.getTest();
        if (currentTest != null) {
            currentTest.info("<span style='color:blue; font-size:12px;'>[DEBUG] " + message + "</span>");
        }
    }

    public static void info(String message) {
        log.info(message);
        ExtentTest currentTest = ExtentReportListener.getTest();
        if (currentTest != null) {
            currentTest.info("<b>[INFO]</b> " + message);
        }
    }

    public static void warn(String message) {
        log.warn(message);
        ExtentTest currentTest = ExtentReportListener.getTest();
        if (currentTest != null) {
            currentTest.warning("<span style='color:orange; font-weight:bold;'>[WARN] " + message + "</span>");
        }
    }

    public static void error(String message, Throwable t) {
        log.error(message, t);
        ExtentTest currentTest = ExtentReportListener.getTest();
        if (currentTest != null) {
            currentTest.fail("<span style='color:red; font-weight:bold;'>[ERROR] " + message + "</span><br>" + t.getMessage());
        }
    }
}