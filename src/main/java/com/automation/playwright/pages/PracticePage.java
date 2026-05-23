package com.automation.playwright.pages;

import com.microsoft.playwright.Page;
import utils.LinkUtils;
import utils.reports.LogUtils;

import java.util.List;

public class PracticePage {
    private final Page page;

    // Explicit container selector matching your provided HTML structure
    private final String mainContainer = "section#main-container";

    public PracticePage(Page page) {
        this.page = page;
    }

    /**
     * Gets the total count of valid links inside the main container.
     */
    public int getNumberOfLinks() {
        LogUtils.info("Calculating total links inside the primary main-container viewport.");
        return getActiveUrls().size();
    }

    /**
     * Leverages the LinkUtils class to scan and check for any broken links in the section.
     */
    public List<String> findBrokenLinks() {
        LogUtils.info("Initiating deep validation run to probe link response networks.");
        List<String> activeUrls = getActiveUrls();
        return LinkUtils.checkBrokenLinks(page, activeUrls);
    }

    /**
     * Helper mapping method to keep code clean and maintainable.
     */
    private List<String> getActiveUrls() {
        return LinkUtils.getAllLinksOnPage(page, mainContainer);
    }
}