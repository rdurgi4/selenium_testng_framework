package com.qa.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.qa.utils.JiraClient;
import com.qa.utils.SlackNotifier;
import org.testng.*;

import java.util.concurrent.atomic.AtomicInteger;

public class TestListener implements ITestListener, ISuiteListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testNode = new ThreadLocal<>();

    private final JiraClient jira = new JiraClient();
    private final SlackNotifier slack = new SlackNotifier();

    private final AtomicInteger total   = new AtomicInteger();
    private final AtomicInteger passed  = new AtomicInteger();
    private final AtomicInteger failed  = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    // ── Suite lifecycle ────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/TestReport.html");
        spark.config().setDocumentTitle("Login Automation Report");
        spark.config().setReportName("Selenium Login Tests");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Framework", "Selenium + TestNG");
        extent.setSystemInfo("Author", "QA Automation");
    }

    @Override
    public void onFinish(ISuite suite) {
        extent.flush();
        slack.sendSuiteResult(total.get(), passed.get(), failed.get(), skipped.get());
    }

    // ── Test lifecycle ─────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        total.incrementAndGet();
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        testNode.set(test);
        test.log(Status.INFO, "Test started");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passed.incrementAndGet();
        testNode.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed.incrementAndGet();
        Throwable cause = result.getThrowable();
        String message    = cause != null ? cause.getMessage() : "No message";
        String stackTrace = cause != null ? stackTraceToString(cause) : "";
        String testName   = result.getMethod().getMethodName();

        testNode.get().log(Status.FAIL, "Test failed: " + message);

        // Create Jira bug and notify Slack
        String jiraKey = jira.createBug(testName, message, stackTrace);
        slack.sendFailureAlert(testName, jiraKey, message);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipped.incrementAndGet();
        testNode.get().log(Status.SKIP, "Test skipped");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String stackTraceToString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append("\tat ").append(el.toString()).append("\n");
            if (sb.length() > 3000) { sb.append("..."); break; }
        }
        return sb.toString();
    }
}
