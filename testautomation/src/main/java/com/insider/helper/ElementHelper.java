package com.insider.helper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class for interacting with web elements.
 * Author: Merve Aslantürkiyeli Demir
 */
public class ElementHelper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JsonReader jsonReader;
    private final JavascriptExecutor js;
    private final boolean highlightEnabled;

    public ElementHelper(WebDriver driver) {
        this.driver = driver;
        this.jsonReader = new JsonReader();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) driver;
        
        String highlightSetting = System.getProperty("highlightElements");
        if (highlightSetting == null) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                Properties props = new Properties();
                props.load(input);
                highlightSetting = props.getProperty("highlightElements", "false");
            } catch (IOException e) {
                highlightSetting = "false";
            }
        }
        this.highlightEnabled = Boolean.parseBoolean(highlightSetting);
    }

    /**
     * Retrieves the locator for a given element name.
     * @param elementName The name of the element.
     * @return The By locator for the element.
     */
    private By getLocator(String elementName) {
        try {
            String locatorType = jsonReader.getLocatorType(elementName);
            String locatorValue = jsonReader.getLocatorValue(elementName);
            
            return switch (locatorType.toLowerCase()) {
                case "xpath" -> By.xpath(locatorValue);
                case "css" -> By.cssSelector(locatorValue);
                case "id" -> By.id(locatorValue);
                case "name" -> By.name(locatorValue);
                default -> throw new IllegalArgumentException("Unsupported locator type: " + locatorType);
            };
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to get locator for element: " + elementName, e);
        }
    }

    /**
     * Highlights a web element by changing its style temporarily.
     * @param element The web element to highlight.
     */
    private void highlightElement(WebElement element) {
        if (!highlightEnabled) {
            return;
        }
        
        String originalStyle = element.getAttribute("style");
        js.executeScript(
                "arguments[0].setAttribute('style', 'border: 2px solid red; background: yellow');",
                element);
        waitForSeconds(1);
        js.executeScript(
            "arguments[0].setAttribute('style', '" + originalStyle + "');",
            element);
    }

    /**
     * Scrolls the page to find the element and highlights it.
     * @param locator The locator of the element.
     * @return True if the element is found and displayed, false otherwise.
     */
    private boolean scrollAndFindElement(By locator) {
        long totalHeight = (Long) js.executeScript("return document.documentElement.scrollHeight");
        int scrollStep = 300;
        long currentPosition = 0;

        while (currentPosition < totalHeight) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
                    waitForSeconds(1);
                    highlightElement(element);
                    return true;
                }
            } catch (Exception ignored) {
                js.executeScript("window.scrollTo({top: " + (currentPosition + scrollStep) + 
                               ", behavior: 'smooth'});");
                currentPosition += scrollStep;
                waitForMilliseconds(300);
            }

            if (currentPosition >= totalHeight) {
                js.executeScript("window.scrollTo({top: 0, behavior: 'smooth'});");
                waitForSeconds(1);
                break;
            }
        }
        return false;
    }

    /**
     * Waits for a specified number of milliseconds.
     * @param milliseconds The number of milliseconds to wait.
     */
    private void waitForMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    /**
     * Waits until the element is visible and clickable
     * @param locator The locator of the element
     * @return The WebElement that is visible and clickable
     */
    private WebElement waitForElementToBeInteractable(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        return element;
    }

    /**
     * Waits for element animations to complete and ensures element is truly clickable
     * @param element The web element to wait for
     */
    private void waitForAnimationToComplete(WebElement element) {
        int attempts = 0;
        int maxAttempts = 5;
        int animationCheckInterval = 200;

        while (attempts < maxAttempts) {
            try {
                String beforeState = element.getAttribute("class") + 
                                   element.getCssValue("transform") + 
                                   element.getCssValue("opacity");
                waitForMilliseconds(animationCheckInterval);
                String afterState = element.getAttribute("class") + 
                                  element.getCssValue("transform") + 
                                  element.getCssValue("opacity");
                
                if (beforeState.equals(afterState)) {
                    return;
                }
            } catch (Exception ignored) {}
            attempts++;
        }
    }

    /**
     * Retries clicking on an element with multiple strategies
     * @param element The web element to click
     * @param elementName Name of the element for logging
     */
    private void retryClick(WebElement element, String elementName) {
        Exception lastException = null;
        String[] clickStrategies = {"normal", "actions", "javascript"};

        for (String strategy : clickStrategies) {
            try {
                waitForAnimationToComplete(element);
                
                switch (strategy) {
                    case "normal" -> element.click();
                    case "actions" -> new Actions(driver).moveToElement(element).click().perform();
                    case "javascript" -> js.executeScript("arguments[0].click();", element);
                }
                
                System.out.printf("✓ Successfully clicked '%s' using %s click%n", elementName, strategy);
                return;
            } catch (Exception e) {
                lastException = e;
                System.out.printf("⚠️ %s click failed for '%s', trying next strategy%n", strategy, elementName);
                waitForMilliseconds(500);
            }
        }
        
        throw new RuntimeException("Failed to click element after trying all strategies: " + elementName, lastException);
    }

    /**
     * Clicks on an element identified by its name.
     * @param elementName The name of the element to click.
     */
    @SuppressWarnings("UseSpecificCatch")
    public void click(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                throw new RuntimeException("Element not found after scrolling: " + elementName);
            }
            WebElement element = waitForElementToBeInteractable(locator);
            highlightElement(element);
            retryClick(element, elementName);
            
            if (driver.getWindowHandles().size() > 1) {
                switchToNewTab();
            }
        } catch (Exception e) {
            String error = String.format("✗ Failed to click element '%s': %s", elementName, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Waits for a specified number of seconds.
     * @param seconds The number of seconds to wait.
     */
    public void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    /**
     * Clicks on an element using JavaScript.
     * @param elementName The name of the element to click.
     */
    @SuppressWarnings("UseSpecificCatch")
    public void clickUsingJS(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                throw new RuntimeException("Element not found after scrolling: " + elementName);
            }
            WebElement element = waitForElementToBeInteractable(locator);
            highlightElement(element);
            js.executeScript("arguments[0].click();", element);
            System.out.printf("✓ Element '%s' clicked using JavaScript successfully%n", elementName);
        } catch (Exception e) {
            String error = String.format("✗ Failed to click element '%s' using JavaScript: %s", elementName, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Moves to an element and clicks it.
     * @param elementName The name of the element to move to and click.
     */
    @SuppressWarnings("UseSpecificCatch")
    public void moveToElementAndClick(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                throw new RuntimeException("Element not found after scrolling: " + elementName);
            }
            WebElement element = waitForElementToBeInteractable(locator);
            highlightElement(element);
            moveToElementAndClick(element);
            System.out.printf("✓ Element '%s' moved to and clicked successfully%n", elementName);
        } catch (Exception e) {
            String error = String.format("✗ An unexpected error occurred while moving to and clicking element '%s': %s", elementName, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Moves to an element and clicks it using JavaScript.
     * @param elementName The name of the element to move to and click using JavaScript.
     */
    @SuppressWarnings("UseSpecificCatch")
    public void moveToElementAndClickWithJs(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                throw new RuntimeException("Element not found after scrolling: " + elementName);
            }
            WebElement element = waitForElementToBeInteractable(locator);
            highlightElement(element);
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            waitForSeconds(1);
            retryClick(element, elementName);
        } catch (Exception e) {
            String error = String.format("✗ An unexpected error occurred while moving to and clicking element '%s' using JavaScript: %s", elementName, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Moves to an element and clicks it using Actions.
     * @param element The web element to move to and click.
     */
    private void moveToElementAndClick(WebElement element) {
        try {
            Actions actions = new Actions(driver);
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            waitForSeconds(1);
            
            actions.moveToElement(element).perform();
            waitForSeconds(1);
            
            actions.click(element).perform();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hover and click element: " + e.getMessage());
        }
    }

    /**
     * Accepts cookies if the cookie banner is present.
     */
    public void acceptCookiesIfPresent() {
        try {
            By cookieLocator = getLocator("acceptCookies");
            WebElement cookieButton = driver.findElement(cookieLocator);
            if (cookieButton.isDisplayed()) {
                cookieButton.click();
                System.out.println("✓ Cookies accepted");
            }
        } catch (Exception e) {
            System.out.println("No cookie banner found or already accepted");
        }
    }

    /**
     * Scrolls the page to the top.
     */
    public void scrollToTop() {
        js.executeScript("window.scrollTo({top: 0, behavior: 'smooth'});");
        waitForSeconds(1);
        System.out.println("✓ Scrolled to the top of the page");
    }

    /**
     * Checks if an element is visible on the screen.
     * @param elementName The name of the element to check.
     * @return True if the element is visible, false otherwise.
     */
    @SuppressWarnings("UseSpecificCatch")
    public boolean isElementVisible(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                String error = String.format("Element '%s' could not be found on the page", elementName);
                System.err.println("✗ " + error);
                throw new AssertionError(error);
            }
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            highlightElement(element);
            System.out.printf("✓ Element '%s' is visible%n", elementName);
            return true;
        } catch (Exception e) {
            String error = String.format("Element '%s' is not visible: %s", elementName, e.getMessage());
            System.err.println("✗ " + error);
            throw new AssertionError(error);
        }
    }

    /**
     * Asserts that an element is visible on the screen.
     * @param elementName The name of the element to check.
     */
    public void assertElementVisible(String elementName) {
        if (!isElementVisible(elementName)) {
            throw new AssertionError("Element '" + elementName + "' is not visible on the screen");
        }
    }

    /**
     * Navigates to the specified URL.
     * @param url The URL to navigate to.
     */
    public void navigateToUrl(String url) {
        try {
            driver.get(url);
            System.out.printf("✓ Navigated to URL: %s%n", url);
            waitForSeconds(2);
        } catch (Exception e) {
            String error = String.format("✗ Failed to navigate to URL '%s': %s", url, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Verifies that all elements in a list contain the expected text.
     * @param elementName The name of the element that returns multiple elements
     * @param expectedText The text that should be present in all elements
     * @return True if all elements contain the expected text, false otherwise
     */
    public boolean verifyTextInElements(String elementName, String expectedText) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                String error = String.format("Elements '%s' could not be found on the page", elementName);
                System.err.println("✗ " + error);
                return false;
            }

            var elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
            
            if (elements.isEmpty()) {
                System.err.printf("✗ No elements found for '%s'%n", elementName);
                return false;
            }

            boolean allMatch = true;
            for (int i = 0; i < elements.size(); i++) {
                WebElement element = elements.get(i);
                js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
                waitForMilliseconds(300);
                
                String actualText = element.getText().trim();
                boolean matches = actualText.contains(expectedText);
                
                if (matches) {
                    highlightElement(element);
                    System.out.printf("✓ Element %d contains expected text '%s': %s%n", 
                        i + 1, expectedText, actualText);
                } else {
                    System.err.printf("✗ Element %d does not contain expected text '%s'. Actual text: %s%n", 
                        i + 1, expectedText, actualText);
                    allMatch = false;
                }
                waitForMilliseconds(500);
            }
            
            return allMatch;
        } catch (Exception e) {
            String error = String.format("✗ Error while verifying text in elements '%s': %s", 
                elementName, e.getMessage());
            System.err.println(error);
            return false;
        }
    }

    /**
     * Moves mouse over an element and waits for any hover effects to appear
     * @param elementName The name of the element to hover over
     */
    @SuppressWarnings("UseSpecificCatch")
    public void hoverElement(String elementName) {
        By locator = getLocator(elementName);
        try {
            if (!scrollAndFindElement(locator)) {
                throw new RuntimeException("Element not found after scrolling: " + elementName);
            }
            
            WebElement element = waitForElementToBeInteractable(locator);
            highlightElement(element);
            
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            
            waitForMilliseconds(300);
            
            waitForAnimationToComplete(element);
            
            System.out.printf("✓ Successfully hovered over element '%s'%n", elementName);
            
        } catch (Exception e) {
            String error = String.format("✗ Failed to hover over element '%s': %s", elementName, e.getMessage());
            System.err.println(error);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Verifies if current URL contains the expected domain
     * @param expectedDomain Domain to verify (e.g., "useinsider.com", "careers.useinsider.com")
     * @return True if current URL contains the expected domain, false otherwise
     */
    public boolean verifyDomain(String expectedDomain) {
        try {
            waitForSeconds(1);
            String currentUrl = driver.getCurrentUrl();
            
            boolean isDomainValid = currentUrl.toLowerCase().contains(expectedDomain.toLowerCase());
            
            if (isDomainValid) {
                System.out.printf("✓ Current URL '%s' contains expected domain '%s'%n", 
                    currentUrl, expectedDomain);
            } else {
                System.err.printf("✗ Current URL '%s' does not contain expected domain '%s'%n", 
                    currentUrl, expectedDomain);
            }
            
            return isDomainValid;
        } catch (Exception e) {
            String error = String.format("✗ Error while verifying domain '%s': %s", 
                expectedDomain, e.getMessage());
            System.err.println(error);
            return false;
        }
    }

    /**
     * Switches to the newly opened tab and waits for page load
     * @return true if switch successful, false otherwise
     */
    public boolean switchToNewTab() {
        try {
            String originalWindow = driver.getWindowHandle();
            waitForSeconds(2);
            
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
                    
                    System.out.printf("✓ Switched to new tab: %s%n", driver.getCurrentUrl());
                    return true;
                }
            }
            
            System.err.println("✗ No new tab found to switch to");
            return false;
            
        } catch (Exception e) {
            String error = "✗ Failed to switch to new tab: " + e.getMessage();
            System.err.println(error);
            return false;
        }
    }

    /**
     * Takes screenshot and saves it with timestamp and test name
     * @param testName Name of the test case
     * @return Path to the saved screenshot
     */
    @SuppressWarnings("UseSpecificCatch")
    public String takeScreenshot(String testName) {
        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("screenshot_%s_%s.png", testName, timestamp);
            String filePath = "test-output/screenshots/" + fileName;

            java.io.File directory = new java.io.File("test-output/screenshots");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            java.io.File screenshotFile = ((org.openqa.selenium.TakesScreenshot) driver)
                .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            
            org.apache.commons.io.FileUtils.copyFile(screenshotFile, new java.io.File(filePath));
            
            System.out.printf("✓ Screenshot saved: %s%n", filePath);
            return filePath;
        } catch (Exception e) {
            String error = String.format("✗ Failed to take screenshot: %s", e.getMessage());
            System.err.println(error);
            return null;
        }
    }
}
