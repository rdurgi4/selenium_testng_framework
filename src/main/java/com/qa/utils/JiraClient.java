package com.qa.utils;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;

public class JiraClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient();
    private final String baseUrl;
    private final String authHeader;
    private final String projectKey;

    public JiraClient() {
        this.baseUrl    = ConfigReader.get("JIRA_URL");
        this.projectKey = ConfigReader.get("JIRA_PROJECT_KEY");
        String credentials = ConfigReader.get("JIRA_EMAIL") + ":" + ConfigReader.get("JIRA_API_TOKEN");
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Creates a Bug issue in Jira and returns the created issue key (e.g. QA-42).
     */
    public String createBug(String testName, String failureMessage, String stackTrace) {
        JsonObject fields = new JsonObject();

        JsonObject project = new JsonObject();
        project.addProperty("key", projectKey);
        fields.add("project", project);

        fields.addProperty("summary", "[AUTO] Login test failed: " + testName);

        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Bug");
        fields.add("issuetype", issueType);

        String description = String.format(
            "h2. Automated test failure\n\n" +
            "*Test:* %s\n\n" +
            "*Failure reason:*\n{code}\n%s\n{code}\n\n" +
            "*Stack trace:*\n{code}\n%s\n{code}",
            testName,
            truncate(failureMessage, 2000),
            truncate(stackTrace, 4000)
        );
        fields.addProperty("description", description);

        JsonObject body = new JsonObject();
        body.add("fields", fields);

        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder()
            .url(baseUrl + "/rest/api/2/issue")
            .addHeader("Authorization", authHeader)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build();

        try (Response response = http.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                System.err.println("[JiraClient] Failed to create issue (" + response.code() + "): " + responseBody);
                return null;
            }
            // Extract issue key from {"id":"...","key":"QA-42","self":"..."}
            String issueKey = responseBody.replaceAll(".*\"key\":\"([^\"]+)\".*", "$1");
            System.out.println("[JiraClient] Created issue: " + issueKey);
            return issueKey;
        } catch (IOException e) {
            System.err.println("[JiraClient] IOException creating issue: " + e.getMessage());
            return null;
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}
