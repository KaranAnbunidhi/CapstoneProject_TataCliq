package utils;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class Base {

    protected Properties prop;
    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static ExtentReports extentReports;

    public void launchBrowser() {

        // ✅ Ensure reports folder exists
        new File("reports").mkdirs();

        // ✅ Attach Extent reporter
        ExtentSparkReporter spark =
                new ExtentSparkReporter("reports/ExtentReport.html");

        extentReports = new ExtentReports();
        extentReports.attachReporter(spark);

        prop = PropertyReader.readProperty();
        String browser = prop.getProperty("Browser");
        String url = prop.getProperty("URL");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_leak_detection", false);

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--headless");// headless mode
            driver = new ChromeDriver(options); 
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        } else if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        } else if (browser.equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        } else {
            throw new RuntimeException("Invalid browser name: " + browser);
        }

        driver.manage().window().maximize();
        driver.get(url);
    }

    // ✅ Runs ONCE after all tests
    @AfterSuite(alwaysRun = true)
    public void tearDown() {

        // 1️⃣ Flush report first
        if (extentReports != null) {
            extentReports.flush();
        }

        // 2️⃣ Send ONLY Extent report by mail
        String reportPath =
            System.getProperty("user.dir") + "/reports/ExtentReport.html";

        ExtentReportMailer.sendExtentReport(reportPath);

        // 3️⃣ Quit browser
        if (driver != null) {
            driver.quit();
        }
    }
}
