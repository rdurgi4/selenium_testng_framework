package com.qa.tests;

import com.qa.base.BaseTest;
import com.qa.pages.LoginPage;
import com.qa.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(description = "Valid credentials should land on the dashboard")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(
            ConfigReader.get("APP_USERNAME"),
            ConfigReader.get("APP_PASSWORD")
        );
        Assert.assertTrue(
            loginPage.isLoginSuccessful(),
            "Expected to be logged in but logout link was not found"
        );
    }

    @Test(description = "Wrong password should display an error message")
    public void testInvalidPassword() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(ConfigReader.get("APP_USERNAME"), "wrong_password_123!");
        Assert.assertTrue(
            loginPage.isErrorDisplayed(),
            "Expected an error message for invalid credentials"
        );
    }

    @Test(description = "Empty credentials should display a validation error")
    public void testEmptyCredentials() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login("", "");
        Assert.assertTrue(
            loginPage.isErrorDisplayed(),
            "Expected a validation error for empty username/password"
        );
    }

    @Test(description = "Non-existent username should display an error message")
    public void testUnknownUsername() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login("no_such_user_xyz@example.com", "somePassword1!");
        Assert.assertTrue(
            loginPage.isErrorDisplayed(),
            "Expected an error for unknown username"
        );
    }

    @Test(description = "SQL injection attempt should be rejected")
    public void testSqlInjectionInput() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login("' OR '1'='1", "' OR '1'='1");
        Assert.assertFalse(
            loginPage.isLoginSuccessful(),
            "SQL injection input must NOT result in a successful login"
        );
    }
}
