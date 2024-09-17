import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class NhlApiTests {

    @Test
    public static void GetNumberOfTeams() {

        JsonPath jsonPath = given()
                .baseUri("https://qa-assignment.dev1.whalebone.io/api")
                .contentType(ContentType.JSON)
                .when()
                .get("/teams").jsonPath();

        List<Map<String, Object>> teams = jsonPath.getList("teams");
        assert teams.size() == 32;
    }

    @Test
    public static void VerifyOldestTeam() {

        JsonPath jsonPath = given()
                .baseUri("https://qa-assignment.dev1.whalebone.io/api")
                .contentType(ContentType.JSON)
                .when()
                .get("/teams").jsonPath();

        List<Map<String, Object>> teams = jsonPath.getList("teams");
        Map<String, Object> oldestTeam = teams.stream()
                .min(Comparator.comparing(entry -> (int) entry.get("founded")))
                .orElse(null);

        assert (oldestTeam.get("name")).equals("Montreal Canadiens");

    }

    @Test
    public static void VerifyCityWithMoreTeams() {
        JsonPath jsonPath = given()
                .baseUri("https://qa-assignment.dev1.whalebone.io/api")
                .contentType(ContentType.JSON)
                .when()
                .get("/teams").jsonPath();

        List<Map<String, Object>> teams = jsonPath.getList("teams");

        Map<Object, Long> cityTeamCount = teams.stream()
                .collect(Collectors.groupingBy(team -> team.get("location"), Collectors.counting()));
        List<Object> citiesWithMultipleTeams = cityTeamCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        assert citiesWithMultipleTeams.size() == 1;
        assert citiesWithMultipleTeams.get(0).equals("New York");

    }

    @Test
    public static void VerifyMetropolitanDivision() {

        JsonPath jsonPath = given()
                .baseUri("https://qa-assignment.dev1.whalebone.io/api")
                .contentType(ContentType.JSON)
                .when()
                .get("/teams").jsonPath();

        List<Map<String, Object>> teams = jsonPath.getList("teams");

        List<Map<String, Object>> teamsInMetropolitanDivision = teams.stream()
                .filter(team -> ((Map<String, String>) team.get("division")).get("name").equals("Metropolitan"))
                        .collect(Collectors.toList());

        List<String> testTeams = new ArrayList<>();
        testTeams.add("Carolina Hurricanes");
        testTeams.add("Columbus Blue Jackets");
        testTeams.add("New Jersey Devils");
        testTeams.add("New York Islanders");
        testTeams.add("New York Rangers");
        testTeams.add("Philadelphia Flyers");
        testTeams.add("Pittsburgh Penguins");
        testTeams.add("Washington Capitals");
        assert teamsInMetropolitanDivision.size() == 8;
        for (Map<String, Object> team : teamsInMetropolitanDivision) {
            assert testTeams.contains((String) team.get("name"));
        }
    }

    @Test
    public static void VerifyOldestTeamCanadianPlayers() {

        JsonPath jsonPath = given()
                .baseUri("https://qa-assignment.dev1.whalebone.io/api")
                .contentType(ContentType.JSON)
                .when()
                .get("/teams").jsonPath();

        List<Map<String, Object>> teams = jsonPath.getList("teams");
        Map<String, Object> oldestTeam = teams.stream()
                .min(Comparator.comparing(entry -> (int) entry.get("founded")))
                .orElse(null);

        assert (oldestTeam.get("name")).equals("Montreal Canadiens");

        String candediensUrl = (oldestTeam.get("officialSiteUrl") + "roster");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate(candediensUrl);
            String xpathString = "xpath=//h5[text()='Attaquants']//following-sibling::div//tbody/tr[";
            int numberOfUsPlayers = 0;
            int numberOfCanPlayers = 0;
            for(int i = 1; i<=28; i++){

                if(((page.locator(xpathString + i + "]/td[7]")).textContent().contains("USA"))){
                    numberOfUsPlayers++;
                } else if (((page.locator(xpathString + i + "]/td[7]")).textContent().contains("CAN"))){
                    numberOfCanPlayers++;
                }
            }
            xpathString = "xpath=//h5[text()='Défenseurs']//following-sibling::div//tbody/tr[";
            for(int i = 1; i<=11; i++){
                if(((page.locator(xpathString + i + "]/td[7]")).textContent().contains("USA"))){
                    numberOfUsPlayers++;
                } else if (((page.locator(xpathString + i + "]/td[7]")).textContent().contains("CAN"))){
                    numberOfCanPlayers++;
                }
            }

            xpathString = "xpath=//h5[text()='Gardiens']//following-sibling::div//tbody/tr[";
            for(int i = 1; i<=4; i++){
                if(((page.locator(xpathString + i + "]/td[7]")).textContent().contains("USA"))){
                    numberOfUsPlayers++;
                } else if (((page.locator(xpathString + i + "]/td[7]")).textContent().contains("CAN"))){
                    numberOfCanPlayers++;
                }
            }
            browser.close();
            Assert.assertEquals(numberOfCanPlayers, 27);
            Assert.assertEquals(numberOfUsPlayers, 7);
            Assert.assertTrue(numberOfUsPlayers < numberOfCanPlayers);
            browser.close();
        }
    }
}
