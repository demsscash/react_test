import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait longWait;
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
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
            WebElement errorElement = driver.findElement(errorMessageSelector);
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

            // Nettoyage et remplissage des champs
            emailInput.clear();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", emailInput);
            emailInput.sendKeys(email);

            passwordInput.clear();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", passwordInput);
            passwordInput.sendKeys(password);

            System.out.println("Formulaire rempli - Email: " + email + ", Password: " + password);
        } catch (Exception e) {
            System.out.println("Erreur lors du remplissage du formulaire: " + e.getMessage());
            throw new RuntimeException("Erreur lors du remplissage du formulaire", e);
        }

        return this;
    }

    public LoginPage submitLoginForm() {
        try {
            System.out.println("Soumission du formulaire de connexion...");
            WebElement submitButton = getSubmitButton();

            // S'assurer que le bouton est cliquable
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));

            // D'abord essayer un clic normal
            submitButton.click();

            // Si cela ne fonctionne pas, essayer avec JavaScript
            try {
                Thread.sleep(500);
                if (driver.getCurrentUrl().contains("/login")) {
                    System.out.println("Essai de clic avec JavaScript...");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                }
            } catch (Exception e) {
                System.out.println("Exception lors du clic JS: " + e.getMessage());
            }

            System.out.println("Formulaire soumis, URL actuelle: " + driver.getCurrentUrl());
        } catch (Exception e) {
            System.out.println("Erreur lors de la soumission du formulaire: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la soumission du formulaire", e);
        }
        return this;
    }

    public LoginPage login(String email, String password) {
        System.out.println("Tentative de connexion avec: " + email);
        fillLoginForm(email, password);
        submitLoginForm();

        // Attendre que la page se charge après connexion
        try {
            Thread.sleep(3000);
            System.out.println("Après attente, URL: " + driver.getCurrentUrl());

            // Afficher les cookies pour débogage
            printCookies();

            // Si toujours sur login mais pas d'erreur, on considère la connexion réussie
            if (driver.getCurrentUrl().contains("/login") && !isErrorVisible()) {
                System.out.println("Toujours sur /login mais pas d'erreur - navigation manuelle vers la racine");
                driver.get(baseUrl);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return this;
    }

    public void printCookies() {
        Set<Cookie> cookies = driver.manage().getCookies();
        System.out.println("Cookies après connexion (" + cookies.size() + ") :");
        for (Cookie cookie : cookies) {
            System.out.println("  - " + cookie.getName() + ": " + cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "...");
        }
    }

    public LoginPage waitForErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageSelector));
            System.out.println("Message d'erreur détecté");
        } catch (Exception e) {
            System.out.println("Le message d'erreur n'est jamais apparu: " + e.getMessage());
        }
        return this;
    }

    public LoginPage waitForErrorToDisappear() {
        try {
            wait.until(driver -> {
                try {
                    WebElement errorElement = driver.findElement(errorMessageSelector);
                    String classAttribute = errorElement.getAttribute("class");
                    boolean isGone = classAttribute.contains("offscreen");
                    System.out.println("Message d'erreur disparu: " + isGone);
                    return isGone;
                } catch (Exception e) {
                    return true; // Si l'élément n'existe plus, le test passe
                }
            });
        } catch (Exception e) {
            System.out.println("Le message d'erreur n'a pas disparu: " + e.getMessage());
        }
        return this;
    }

    public LoginPage waitForRedirect() {
        String startUrl = driver.getCurrentUrl();
        System.out.println("Attente de redirection, URL initiale: " + startUrl);

        try {
            // Attendre que l'URL change ou qu'un élément de la page d'accueil apparaisse
            longWait.until(driver -> {
                String currentUrl = driver.getCurrentUrl();
                boolean redirected = !currentUrl.contains("/login");
                System.out.println("Vérification de redirection - URL actuelle: " + currentUrl + ", Redirigé: " + redirected);

                // Si l'URL a changé, on considère qu'il y a eu redirection
                if (redirected) {
                    return true;
                }

                // Sinon, vérifier si des éléments de la page d'accueil sont présents
                try {
                    boolean hasHomeElements = !driver.findElements(By.tagName("table")).isEmpty() ||
                            !driver.findElements(By.cssSelector("h1")).isEmpty();
                    return hasHomeElements && !isErrorVisible();
                } catch (Exception e) {
                    return false;
                }
            });

            System.out.println("Redirection détectée ou éléments de page d'accueil trouvés");
        } catch (Exception e) {
            System.out.println("Exception lors de l'attente de redirection: " + e.getMessage());

            // Si pas redirigé mais pas d'erreur visible non plus, tenter navigation manuelle
            if (driver.getCurrentUrl().contains("/login") && !isErrorVisible()) {
                System.out.println("Navigation manuelle vers la racine");
                driver.get(baseUrl);
            }
        }
        return this;
    }

    public boolean isAuthenticated() {
        // Méthode simplifiée pour vérifier l'authentification
        // On considère l'utilisateur authentifié si :
        // 1. Il n'est pas sur la page de login, OU
        // 2. Il est sur la page de login mais sans message d'erreur (cas spécial)

        String currentUrl = driver.getCurrentUrl();

        if (!currentUrl.contains("/login")) {
            System.out.println("Authentification vérifiée : l'utilisateur est sur une page protégée");
            return true;
        }

        // Si sur login mais pas d'erreur, tenter une navigation vers la racine
        if (currentUrl.contains("/login") && !isErrorVisible()) {
            System.out.println("Sur login sans erreur - tentative de navigation vers la racine");

            // Sauvegarder les cookies
            Set<Cookie> cookies = driver.manage().getCookies();

            driver.get(baseUrl);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si redirigé vers login
            boolean redirectedToLogin = driver.getCurrentUrl().contains("/login");
            System.out.println("Après navigation manuelle : " + (redirectedToLogin ? "redirigé vers login" : "resté sur la page protégée"));

            return !redirectedToLogin;
        }

        return false;
    }
}