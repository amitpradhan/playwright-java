package day3;

import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.List;

public class HerokuAppDemoAddDelete {
    public static void main(String[] args) {
        try(Playwright playwright = Playwright.create()){
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://the-internet.herokuapp.com/add_remove_elements/");
            page.getByText("Add Element").click();

            //delete if present
            Locator deleteBtn = page.getByText("Delete");
            if(deleteBtn.isVisible())
                deleteBtn.click();
            page.waitForTimeout(2000);
            System.out.println("Delete btn added and deleted..");
            page.getByText("Add Element").click();
            page.getByText("Add Element").click();
            page.getByText("Add Element").click();
            System.out.println("Three Delete btn added");
            page.waitForTimeout(2000);

            Locator deleteBtns = page.getByText("Delete");
            int noOfDeleteBtns = deleteBtns.count();
            if(noOfDeleteBtns == 3)
                System.out.println("Test passed");
            else
                System.out.println("Test failed");

            System.out.println("Click on each delete btns..");
            for(int i= 0;i<deleteBtns.count();i++)
                deleteBtns.nth(0).click();
//            while (deleteBtns.first().isVisible()){
//                deleteBtns.click();
//            }
//
//            for (int i = 0; i < noOfDeleteBtns; i++) {
//                // We click .first() repeatedly because as buttons are deleted,
//                // the list of elements on the page changes.
//                deleteBtns.first().click();
//            }
            System.out.println("All delete btns deleted..");

            page.getByText("Add Element").click();
            page.getByText("Add Element").click();
            page.getByText("Add Element").click();
            System.out.println("Three Delete btn added");

//            List<Locator> buttons = page.getByText("Delete").all();
//            System.out.println("No of delete buttons->"+buttons.size());
//            for (Locator button : buttons) {
//                button.click();
//            }

             deleteBtns = page.getByText("Delete");
            while (deleteBtns.count() > 0) {
                deleteBtns.first().click();
            }

            page.waitForTimeout(3000);





    }
}}
