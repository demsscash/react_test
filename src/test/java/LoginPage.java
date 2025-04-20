import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    // Sélecteurs
    private final By titleSelector = By.cssSelector(".connexion h2");
    private final By emailInputSelector = By.id("username");
    private final By passwordInputSelector = By.id("password");
    private final By submitButtonSelector = By.cssSelector(".form-submit");
    private final By errorMessageSelector = By.className("errmsg");

    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public LoginPage open() {
        driver.get(baseUrl + "/login");
        return this;
    }

    public WebElement getEmailInput() {
        try {
            return driver.findElement(emailInputSelector);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Le champ email n'a pas été trouvé sur la page", e);
        }
    }

    public WebElement getPasswordInput() {
        try {
            return driver.findElement(passwordInputSelector);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Le champ mot de passe n'a pas été trouvé sur la page", e);
        }
    }

    public WebElement getSubmitButton() {
        try {
            return driver.findElement(submitButtonSelector);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Le bouton de soumission n'a pas été trouvé sur la page", e);
        }
    }

    public String getTitle() {
        try {
            return driver.findElement(titleSelector).getText();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Le titre n'a pas été trouvé sur la page", e);
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getActiveElementId() {
        try {
            return driver.switchTo().activeElement().getAttribute("id");
        } catch (Exception e) {
            return "";
        }
    }

    public String getErrorMessage() {
        try {
            WebElement errorElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(errorMessageSelector)
            );
            return errorElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorVisible() {
        try {
            WebElement errorElement = driver.findElement(errorMessageSelector);
            String classAttribute = errorElement.getAttribute("class");
            return !classAttribute.contains("offscreen");
        } catch (Exception e) {
            return false;
        }
    }

    public LoginPage fillLoginForm(String email, String password) {
        try {
            WebElement emailInput = getEmailInput();
            WebElement passwordInput = getPasswordInput();

            emailInput.clear();
            emailInput.sendKeys(email);

            passwordInput.clear();
            passwordInput.sendKeys(password);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du remplissage du formulaire", e);
        }

        return this;
    }

    public LoginPage submitLoginForm() {
        try {
            getSubmitButton().click();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la soumission du formulaire", e);
        }
        return this;
    }

    public LoginPage login(String email, String password) {
        fillLoginForm(email, password);
        submitLoginForm();
        return this;
    }

    public LoginPage waitForErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageSelector));
        } catch (Exception e) {
            throw new RuntimeException("Le message d'erreur n'est jamais apparu", e);
        }
        return this;
    }

    public LoginPage waitForErrorToDisappear() {
        try {
            wait.until(driver -> {
                try {
                    WebElement errorElement = driver.findElement(errorMessageSelector);
                    String classAttribute = errorElement.getAttribute("class");
                    return classAttribute.contains("offscreen");
                } catch (Exception e) {
                    return true; // Si l'élément n'existe plus, le test passe
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Le message d'erreur n'a pas disparu", e);
        }
        return this;
    }

    public LoginPage waitForRedirect() {
        try {
            wait.until(driver -> {
                String currentUrl = driver.getCurrentUrl();
                return !currentUrl.contains("/login");
            });
        } catch (Exception e) {
            throw new RuntimeException("La redirection n'a pas eu lieu après la connexion", e);
        }
        return this;
    }
}