package com.insider.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.WebDriver;

import com.insider.helper.ElementHelper;
import com.insider.manager.DriverManager;

@ExtendWith(BaseTest.ScreenshotOnFailureExtension.class)
public abstract class BaseTest {
    protected static WebDriver driver;
    protected static ElementHelper elementHelper;
    protected static Properties properties;

    @BeforeAll
    public static void setUp() {
        loadProperties();
        driver = DriverManager.getDriver();
        driver.get(properties.getProperty("baseUrl"));
        elementHelper = new ElementHelper(driver);
        elementHelper.acceptCookiesIfPresent();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = BaseTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Cannot find config.properties in classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties from classpath", e);
        }
    }

    @AfterAll
    public static void tearDown() {
        DriverManager.quitDriver();
    }

    public static class ScreenshotOnFailureExtension implements TestExecutionExceptionHandler {
        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            if (elementHelper != null) {
                String testName = context.getTestMethod()
                    .map(method -> method.getDeclaringClass().getSimpleName() + "_" + method.getName())
                    .orElse("unknown");
                elementHelper.takeScreenshot(testName);
            }
            throw throwable;
        }
    }
}
