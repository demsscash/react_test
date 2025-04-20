import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AdminPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    // Sélecteurs pour les éléments de la page admin
    private final By adminTitleSelector = By.cssSelector("h1"); // A ajuster selon votre HTML
    private final By navigationLinksSelector = By.cssSelector("nav a"); // A ajuster selon votre HTML

    // Sélecteurs pour les pages admin spécifiques
    private final By medecinsLinkSelector = By.cssSelector("a[href*='medecins']");
    private final By patientsLinkSelector = By.cssSelector("a[href*='patients']");
    private final By secretairesLinkSelector = By.cssSelector("a[href*='secretaires']");
    private final By cliniquesLinkSelector = By.cssSelector("a[href*='cliniques']");
    private final By kiosksLinkSelector = By.cssSelector("a[href*='kiosks']");

    public AdminPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public AdminPage goToAdminPage() {
        driver.get(baseUrl + "/admin");
        return this;
    }

    public AdminPage goToMedecinsPage() {
        driver.get(baseUrl + "/medecins");
        return this;
    }

    public AdminPage goToPatientsPage() {
        driver.get(baseUrl + "/patients");
        return this;
    }

    public AdminPage goToSecretairesPage() {
        driver.get(baseUrl + "/secretaires");
        return this;
    }

    public AdminPage goToCliniksPage() {
        driver.get(baseUrl + "/cliniques");
        return this;
    }

    public AdminPage goToKiosksPage() {
        driver.get(baseUrl + "/kiosks");
        return this;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isOnAdminPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/admin"));
            return driver.getCurrentUrl().contains("/admin");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canAccessMedecinsPage() {
        try {
            goToMedecinsPage();
            wait.until(ExpectedConditions.urlContains("/medecins"));
            return driver.getCurrentUrl().contains("/medecins");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canAccessPatientsPage() {
        try {
            goToPatientsPage();
            wait.until(ExpectedConditions.urlContains("/patients"));
            return driver.getCurrentUrl().contains("/patients");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canAccessSecretairesPage() {
        try {
            goToSecretairesPage();
            wait.until(ExpectedConditions.urlContains("/secretaires"));
            return driver.getCurrentUrl().contains("/secretaires");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canAccessCliniquesPage() {
        try {
            goToCliniksPage();
            wait.until(ExpectedConditions.urlContains("/cliniques"));
            return driver.getCurrentUrl().contains("/cliniques");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canAccessKiosksPage() {
        try {
            goToKiosksPage();
            wait.until(ExpectedConditions.urlContains("/kiosks"));
            return driver.getCurrentUrl().contains("/kiosks");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUserRedirectedToUnauthorized() {
        try {
            wait.until(ExpectedConditions.urlContains("/unauthorized"));
            return driver.getCurrentUrl().contains("/unauthorized");
        } catch (Exception e) {
            return false;
        }
    }

    public List<WebElement> getNavigationLinks() {
        return driver.findElements(navigationLinksSelector);
    }

    public boolean areAdminLinksVisible() {
        try {
            return driver.findElement(medecinsLinkSelector).isDisplayed() &&
                    driver.findElement(patientsLinkSelector).isDisplayed() &&
                    driver.findElement(secretairesLinkSelector).isDisplayed() &&
                    driver.findElement(cliniquesLinkSelector).isDisplayed() &&
                    driver.findElement(kiosksLinkSelector).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}