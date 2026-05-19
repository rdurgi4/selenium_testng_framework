package com.qa.utils;

import okhttp3.*;

import java.io.IOException;

public class SlackNotifier {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient http = new OkHttpClient();
    private final String webhookUrl;

    public SlackNotifier() {
        this.webhookUrl = ConfigReader.get("SLACK_WEBHOOK_URL");
    }

    public void sendFailureAlert(String testName, String jiraKey, String failureMessage) {
        String jiraLink = jiraKey != null
            ? "<" + ConfigReader.get("JIRA_URL") + "/browse/" + jiraKey + "|" + jiraKey + ">"
            : "_Jira issue not created_";

        String payload = String.format(
            "{" +
            "\"attachments\":[{" +
            "\"color\":\"#FF0000\"," +
            "\"title\":\":x: Login Test Failed\"," +
            "\"fields\":[" +
            "{\"title\":\"Test\",\"value\":\"%s\",\"short\":true}," +
            "{\"title\":\"Jira Issue\",\"value\":\"%s\",\"short\":true}," +
            "{\"title\":\"Failure\",\"value\":\"%s\",\"short\":false}" +
            "]," +
            "\"footer\":\"Selenium Automation | %s\"" +
            "}]" +
            "}",
            escape(testName),
            jiraLink,
            escape(truncate(failureMessage, 300)),
            java.time.LocalDateTime.now()
        );

        post(payload);
    }

    public void sendSuiteResult(int total, int passed, int failed, int skipped) {
        String color = failed > 0 ? "#FF0000" : "#36A64F";
        String status = failed > 0 ? ":x: Suite Completed with Failures" : ":white_check_mark: Suite Passed";

        String payload = String.format(
            "{" +
            "\"attachments\":[{" +
            "\"color\":\"%s\"," +
            "\"title\":\"%s\"," +
            "\"fields\":[" +
            "{\"title\":\"Total\",\"value\":\"%d\",\"short\":true}," +
            "{\"title\":\"Passed\",\"value\":\"%d\",\"short\":true}," +
            "{\"title\":\"Failed\",\"value\":\"%d\",\"short\":true}," +
            "{\"title\":\"Skipped\",\"value\":\"%d\",\"short\":true}" +
            "]," +
            "\"footer\":\"Selenium Automation | %s\"" +
            "}]" +
            "}",
            color, status, total, passed, failed, skipped,
            java.time.LocalDateTime.now()
        );

        post(payload);
    }

    private void post(String payload) {
        RequestBody body = RequestBody.create(payload, JSON);
        Request request = new Request.Builder().url(webhookUrl).post(body).build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("[SlackNotifier] Failed (" + response.code() + ")");
            }
        } catch (IOException e) {
            System.err.println("[SlackNotifier] IOException: " + e.getMessage());
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
