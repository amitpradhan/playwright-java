package com.automation.playwright.pages;

import com.microsoft.playwright.Page;
import utils.reports.LogUtils;

public class HomePage {
    private final Page page;

    // Locators using precise text-matching and target lists
    private final String logoutButton = "a:has-text('Log out')";
    private final String menuContainer = "ul#menu-primary-items";

    public HomePage(Page page) {
        this.page = page;
    }

    /**
     * Checks whether the Log out button is displayed on the viewport.
     */
    public boolean isLogoutButtonDisplayed() {
        LogUtils.trace("Entering isLogoutButtonDisplayed() check.");
        boolean isVisible = page.isVisible(logoutButton);
        LogUtils.info("Log out button visibility status: " + isVisible);
        return isVisible;
    }

    /**
     * Clicks the Logout button to return to the sign-in portal.
     */
    public void clickLogout() {
        LogUtils.info("Clicking the Log out button.");
        page.click(logoutButton);
    }

    /**
     * Validates if a specific navigation top menu is visible on the DOM.
     * @param menuName The text of the menu (e.g., "Home", "Practice", "Courses", "Blog", "Contact")
     */
    public boolean isMenuDisplayed(String menuName) {
        LogUtils.trace("Checking visibility for navigation item: " + menuName);
        // Generates a locator that pins down the specific text inside the navigation list
        String menuLocator = menuContainer + " a:has-text('" + menuName + "')";
        boolean isVisible = page.isVisible(menuLocator);
        LogUtils.info("Menu '" + menuName + "' display status: " + isVisible);
        return isVisible;
    }

    /**
     * Clicks on a specific navigation menu item.
     * @param menuName The exact visible text of the menu anchor
     */
    public void clickMenu(String menuName) {
        String menuLocator = menuContainer + " a:has-text('" + menuName + "')";
        if (isMenuDisplayed(menuName)) {
            LogUtils.info("Navigating away by clicking menu link: " + menuName);
            page.click(menuLocator);
        } else {
            LogUtils.warn("Action Aborted: Menu item '" + menuName + "' was not actionable or visible.");
        }
    }
}