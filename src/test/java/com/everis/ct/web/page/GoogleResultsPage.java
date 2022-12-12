package com.everis.ct.web.page;

import com.everis.ct.web.base.WebBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleResultsPage extends WebBase {

    @FindBy(xpath = "//div[@id='result-stats']")
    protected WebElement resultsLabel;

    public String getSearchResults() {
        return getText(resultsLabel);
    }
}
