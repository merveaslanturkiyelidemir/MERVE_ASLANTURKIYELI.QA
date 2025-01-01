package com.insider.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CareerInfoTest extends BaseTest {

    @Test
    @DisplayName("Verify Career Page Elements")  
    public void verifyCareerPageElements() {  
        assertDoesNotThrow(() -> elementHelper.click("company"),
                "Clicking on 'Company' should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.click("career"),
                "Clicking on 'Careers' link should not throw an exception");

        assertDoesNotThrow(() -> elementHelper.moveToElementAndClickWithJs("seeAllTeams"),
                "Clicking on 'See All Teams' should not throw an exception");
            
        assertDoesNotThrow(() -> elementHelper.isElementVisible("AllTeamsBlock"),
                "'All Teams' block should be visible");

        assertDoesNotThrow(() -> elementHelper.isElementVisible("careerLocationBlock"),
                "'Career Location' block should be visible");

        assertDoesNotThrow(() -> elementHelper.isElementVisible("lifeAtInsiderBlock"),
                "'Life at Insider' block should be visible");

    }
}
