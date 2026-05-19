# Selenium Login Automation Framework

Java + Selenium 4 + TestNG framework with ExtentReports, Jira bug creation, and Slack notifications on test failure.

## Setup

1. **Clone and configure**
   ```bash
   cp config.properties.template config.properties
   # Edit config.properties with your APP_URL, credentials, Jira token, and Slack webhook
   ```

2. **Install dependencies**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

## Project structure

```
src/
  main/java/com/qa/
    base/        BaseTest.java        — WebDriver setup/teardown (ThreadLocal)
    pages/       LoginPage.java       — Page Object for the login screen
    utils/       ConfigReader.java    — Reads config.properties
                 JiraClient.java      — Creates Jira bugs via REST API
                 SlackNotifier.java   — Posts to Slack via Incoming Webhook
    listeners/   TestListener.java    — TestNG + ExtentReports hooks
  test/java/com/qa/tests/
    LoginTest.java                    — 5 login scenarios
  test/resources/
    testng.xml                        — Suite definition
reports/
  TestReport.html                     — Generated after each run
```

## On failure

Each failing test automatically:
1. Logs the failure in ExtentReports (`reports/TestReport.html`)
2. Creates a **Bug** in Jira under `JIRA_PROJECT_KEY`
3. Posts a red alert to the Slack channel via `SLACK_WEBHOOK_URL`

At suite end a summary (total / passed / failed / skipped) is posted to Slack.

## Configuration reference

| Key | Description |
|-----|-------------|
| `APP_URL` | Login page URL |
| `APP_USERNAME` / `APP_PASSWORD` | Test account credentials |
| `BROWSER` | `chrome` (default) or `firefox` |
| `HEADLESS` | `true` / `false` |
| `IMPLICIT_WAIT_SECONDS` | Element wait timeout |
| `PAGE_LOAD_TIMEOUT_SECONDS` | Page load timeout |
| `JIRA_URL` | e.g. `https://yourorg.atlassian.net` |
| `JIRA_EMAIL` | Atlassian account email |
| `JIRA_API_TOKEN` | API token from id.atlassian.com |
| `JIRA_PROJECT_KEY` | e.g. `QA` |
| `SLACK_WEBHOOK_URL` | Incoming Webhook URL |
