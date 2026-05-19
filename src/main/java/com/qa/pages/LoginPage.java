package com.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // practicetestautomation.com/practice-test-login/
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.id("submit");
    private final By errorMessage  = By.id("error");
    private final By logoutLink    = By.linkText("Log out");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public LoginPage enterUsername(String username) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        field.clear();
        field.sendKeys(username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        field.clear();
        field.sendKeys(password);
        return this;
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    /** Convenience: fill and submit in one call. */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public boolean isLoginSuccessful() {
        try {
            // Wait for redirect to success page then confirm logout link exists
            wait.until(ExpectedConditions.urlContains("logged-in-successfully"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(logoutLink));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorText() {
        try {
            return driver.findElement(errorMessage).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
