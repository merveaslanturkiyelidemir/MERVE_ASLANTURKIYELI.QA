package com.insider.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InsiderCareerPageTest extends BaseTest {

    @Test
    @DisplayName("Verify QA Career Page and Job Application Process")
    public void filterCareerJobs() {  
        elementHelper.navigateToUrl("https://useinsider.com/careers/quality-assurance/");

        assertDoesNotThrow(() -> elementHelper.click("seeAllQAjobs"), // Element adı düzeltildi
                "Clicking on 'See All QA Jobs' button should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.click("filterByLocation"),
                "Clicking on location filter dropdown should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.click("istanbulTurkeyFilter"),
                "Selecting 'Istanbul, Turkey' from location filter should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.click("blankPage"),
                "Clicking on blank area to close dropdown should not throw an exception");

        assertDoesNotThrow(() -> {
            boolean allJobsValid = elementHelper.verifyTextInElements("jobTitles", "Quality Assurance");
            if (!allJobsValid) {
                throw new AssertionError("Some job titles do not contain 'Quality Assurance'");
            }
        }, "Verifying QA positions in job list should not throw an exception");

        assertDoesNotThrow(() -> {
            boolean allLocationsValid = elementHelper.verifyTextInElements("jobLocations", "Istanbul, Turkiye");
            if (!allLocationsValid) {
                throw new AssertionError("Some job locations are not in Istanbul, Turkey");
            }
        }, "Verifying Istanbul locations in job list should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.hoverElement("jobCard"),
                "Hovering over the first job card should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.click("viewRole"),             
                "Clicking on 'View Role' button should not throw an exception");

        elementHelper.waitForSeconds(2);

        assertDoesNotThrow(() -> {
            boolean isDomainValid = elementHelper.verifyDomain("jobs.lever.co");
            if (!isDomainValid) {
                throw new AssertionError("Job application page domain verification failed");
            }
        }, "Verifying redirect to Lever job application page should not throw an exception");
    }
}
