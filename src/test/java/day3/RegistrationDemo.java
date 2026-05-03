package day3;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

public class RegistrationDemo {
    public static void main(String[] args) {
        try(Playwright playwright = Playwright.create()){
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://demo.automationtesting.in/Register.html");
            //first name
            page.getByPlaceholder("First Name").fill("Playwright");
            //last name
            page.getByPlaceholder("Last Name").fill("Testing..");
            //address
            page.locator("//textarea[@ng-model='Adress']").fill("Kharadi Pune , Maharashtra");
            page.locator("//input[@ng-model='EmailAdress']").fill("abcd@test1234.com");
            page.locator("//input[@ng-model='Phone']").fill("1234567890");
            //radi0
            page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Male").setExact(true)).check();
            //check box
//            page.getByTestId("checkbox1").click();
//            page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Movies")).check();
                page.locator("//input[@id='checkbox1']").check();
//                page.getByText("Hockey").check();
            page.locator("//input[@id='checkbox2']").check();
            //select dropdown
            page.selectOption("//*[@id='Skills']" , "Adobe Photoshop");
            page.locator("#basicBootstrapForm > div:nth-child(10) > div > span > span.selection > span").click();
            page.locator("body > span.select2-container.select2-container--default.select2-container--open > span > span.select2-search.select2-search--dropdown > input").fill("India");
            page.locator("//*[@id=\"select2-country-results\"]/li").click();
            page.waitForTimeout(3000);



        }
    }
}
