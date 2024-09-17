import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaywrightTests {

    @Test
    public static void SampleAppTestSuccess() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Sample App").click();
            page.locator("[name='UserName']").fill("TestUser");
            page.locator("[name='Password']").fill("pwd");
            page.locator("[id='login']").click();
            Assert.assertTrue(page.locator("[id='loginstatus']").textContent().contains("Welcome, TestUser!"));
            browser.close();
        }
    }

    @Test
    public static void SampleAppTestSuccessLogout() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Sample App").click();
            page.locator("[name='UserName']").fill("TestUser");
            page.locator("[name='Password']").fill("pwd");
            page.locator("[id='login']").click();
            Assert.assertTrue(page.locator("[id='loginstatus']").textContent().contains("Welcome, TestUser!"));
            page.locator("[id='login']").click();
            Assert.assertTrue(page.locator("[id='loginstatus']").textContent().contains("User logged out."));
            browser.close();
        }
    }

    @Test
    public static void SampleAppTestFailure() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Sample App").click();
            page.locator("[name='UserName']").fill("TestUser");
            page.locator("[name='Password']").fill("wrongPassword");
            page.locator("[id='login']").click();
            Assert.assertTrue(page.locator("[id='loginstatus']").textContent().contains("Invalid username/password"));
            browser.close();
        }
    }

    @Test
    public static void SampleAppTestFailureEmptyPwd() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Sample App").click();
            page.locator("[name='UserName']").fill("TestUser");
            page.locator("[id='login']").click();
            Assert.assertTrue(page.locator("[id='loginstatus']").textContent().contains("Invalid username/password"));
            browser.close();
        }
    }

    @Test
    public static void LoadDelayTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Load Delay").click();
            page.waitForTimeout(5000);

            Assert.assertTrue(page.getByText("Button Appearing After Delay").isVisible());
            browser.close();
        }
    }

    @Test
    public static void ProgressBarTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("http://uitestingplayground.com/");
            page.getByText("Progress Bar").click();
            page.locator("[id='startButton']").click();
            page.waitForCondition(() -> page.locator("[aria-valuenow='75']").isVisible());
            page.locator("[id='stopButton']").click();

            Assert.assertTrue(page.locator("[id='progressBar']").textContent().equals("75%") |
                    page.locator("[id='progressBar']").textContent().equals("76%"));
            browser.close();
        }
    }
}
