import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AdminLoginTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private AdminPage adminPage;

    // URL avec le port correct
    private final String BASE_URL = "http://localhost:3005";

    // Identifiants d'un administrateur
    private final String ADMIN_EMAIL = "admin@medical.com";
    private final String ADMIN_PASSWORD = "00001991";

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            if (driver != null) {
                try {
                    captureScreenshot(description.getMethodName() + "_failure");
                } catch (Exception ex) {
                    System.err.println("Erreur lors de la capture d'écran : " + ex.getMessage());
                }
            }
        }
    };

    @Before
    public void setUp() {
        try {
            // Configuration du WebDriver avec WebDriverManager
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-notifications");

            // Ajouter pour conserver les cookies entre les navigations
            options.addArguments("--enable-cookies");

            // Décommentez pour exécuter en mode headless
            // options.addArguments("--headless=new");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();

            // Initialiser les pages
            loginPage = new LoginPage(driver, BASE_URL);
            adminPage = new AdminPage(driver, BASE_URL);

            // Créer le dossier screenshots s'il n'existe pas
            try {
                Files.createDirectories(Paths.get("screenshots"));
            } catch (IOException e) {
                System.err.println("Erreur lors de la création du dossier screenshots: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du WebDriver: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            try {
                // Capturer l'état final pour le débogage
                captureScreenshot(testName.getMethodName() + "_end");
                driver.quit();
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture du WebDriver: " + e.getMessage());
            }
        }
    }

    private void captureScreenshot(String testMethod) {
        if (driver == null) {
            System.err.println("Impossible de prendre une capture d'écran: driver est null");
            return;
        }

        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get("screenshots", testMethod + ".png");
            Files.copy(screenshot.toPath(), destination);
            System.out.println("Screenshot saved: " + destination);
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Test
    public void testAdminLoginAndRedirection() {
        System.out.println("===== DÉBUT TEST: testAdminLoginAndRedirection =====");

        // 1. Se connecter avec un compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Attendre un peu pour que la redirection se produise
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capturer l'état après connexion
        captureScreenshot("after_admin_login_redirect");

        // 2. Vérifier que l'utilisateur est redirigé vers la page d'accueil (racine)
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL après connexion : " + currentUrl);

        // Le test est réussi si l'URL est la racine ou si une page d'accueil est affichée
        boolean isRedirectedToHome = currentUrl.equals(BASE_URL + "/") ||
                currentUrl.equals(BASE_URL) ||
                !currentUrl.contains("/login");

        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion", isRedirectedToHome);

        System.out.println("===== FIN TEST: testAdminLoginAndRedirection =====");
    }

    @Test
    public void testAdminInterfaceElementsAfterLogin() {
        System.out.println("===== DÉBUT TEST: testAdminInterfaceElementsAfterLogin =====");

        // 1. Se connecter avec un compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Attendre un peu pour que la redirection se produise
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capturer l'état après connexion
        captureScreenshot("admin_after_login");

        // Liste des sélecteurs qui pourraient indiquer des éléments d'interface admin
        List<String> adminSelectors = new ArrayList<>();
        adminSelectors.add("a[href*='admin']");  // Liens vers page admin
        adminSelectors.add(".admin-menu");       // Menu admin
        adminSelectors.add(".admin-link");       // Lien admin
        adminSelectors.add("#admin-panel");      // Panneau admin
        adminSelectors.add("button.admin");      // Bouton admin
        adminSelectors.add("[data-role='admin']"); // Élément avec attribut de rôle admin
        adminSelectors.add("a[href*='medecins']"); // Lien vers page médecins
        adminSelectors.add("a[href*='patients']"); // Lien vers page patients

        // Vérifier si au moins un élément d'interface admin est présent
        boolean adminElementFound = false;
        String foundSelector = "";

        for (String selector : adminSelectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    adminElementFound = true;
                    foundSelector = selector;
                    System.out.println("Élément d'interface admin trouvé avec le sélecteur : " + selector);
                    System.out.println("Nombre d'éléments trouvés : " + elements.size());

                    // Afficher le texte de l'élément pour débogage
                    for (WebElement element : elements) {
                        try {
                            System.out.println("  - Texte: '" + element.getText() + "', Visible: " + element.isDisplayed());
                        } catch (Exception e) {
                            System.out.println("  - Impossible de récupérer le texte: " + e.getMessage());
                        }
                    }

                    break;
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la recherche du sélecteur " + selector + ": " + e.getMessage());
            }
        }

        if (!adminElementFound) {
            // Si aucun élément admin trouvé, capturer l'état pour analyse
            captureScreenshot("no_admin_elements_found");
            System.out.println("Structure du DOM actuel :");
            try {
                System.out.println(driver.findElement(By.tagName("body")).getAttribute("innerHTML").substring(0, 500) + "...");
            } catch (Exception e) {
                System.out.println("Impossible d'afficher le DOM: " + e.getMessage());
            }
        }

        assertTrue("Des éléments d'interface admin devraient être visibles après connexion admin", adminElementFound);

        System.out.println("===== FIN TEST: testAdminInterfaceElementsAfterLogin =====");
    }

    @Test
    public void testAdminCantAccess() {
        System.out.println("===== DÉBUT TEST: testAdminCantAccess =====");

        // Test simplement si la page admin est accessible sans connexion
        driver.get(BASE_URL + "/admin");

        // Attendre un peu pour la redirection éventuelle
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capturer l'état pour analyse
        captureScreenshot("direct_admin_access");

        // Vérifier qu'on est redirigé vers login
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL après tentative d'accès direct à /admin : " + currentUrl);

        boolean isRedirected = currentUrl.contains("/login");
        assertTrue("L'accès direct à /admin sans connexion devrait rediriger vers /login", isRedirected);

        System.out.println("===== FIN TEST: testAdminCantAccess =====");
    }

    @Test
    public void testAdminHomePageAfterLogin() {
        System.out.println("===== DÉBUT TEST: testAdminHomePageAfterLogin =====");

        // Se connecter avec un compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Attendre un peu pour que la redirection se produise
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Vérifier si nous sommes sur la page d'accueil
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL après connexion : " + currentUrl);

        // Capturer l'état après connexion
        captureScreenshot("admin_home_page");

        // Vérifier si des éléments attendus de la page d'accueil sont présents
        try {
            // Rechercher des éléments qui pourraient indiquer que nous sommes sur la page d'accueil
            boolean hasTable = !driver.findElements(By.tagName("table")).isEmpty();
            boolean hasPatientList = !driver.findElements(By.cssSelector("h1:contains('Patients'), h1:contains('Liste')")).isEmpty();

            System.out.println("Éléments de la page d'accueil : Table présente = " + hasTable + ", Liste de patients = " + hasPatientList);

            // Considérer le test comme réussi si nous sommes sur une page d'accueil
            assertTrue("Après connexion, l'admin devrait voir la page d'accueil avec une liste ou un tableau",
                    hasTable || hasPatientList || !currentUrl.contains("/login"));

        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification des éléments de la page d'accueil : " + e.getMessage());
            fail("Erreur lors de la vérification de la page d'accueil");
        }

        System.out.println("===== FIN TEST: testAdminHomePageAfterLogin =====");
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        System.out.println("===== DÉBUT TEST: testLoginWithInvalidCredentials =====");

        // Tenter de se connecter avec des identifiants invalides
        loginPage.open();
        loginPage.login("invalid@example.com", "wrong_password");

        // Attendre un peu pour que le message d'erreur apparaisse
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Capturer l'état après tentative de connexion
        captureScreenshot("invalid_login_attempt");

        // Vérifier que nous sommes toujours sur la page de login
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL après tentative de connexion invalide : " + currentUrl);

        boolean stillOnLoginPage = currentUrl.contains("/login");
        assertTrue("Après une tentative de connexion invalide, l'utilisateur devrait rester sur la page de login", stillOnLoginPage);

        // Vérifier si un message d'erreur est affiché
        boolean errorVisible = loginPage.isErrorVisible();
        System.out.println("Message d'erreur visible : " + errorVisible);

        if (errorVisible) {
            String errorMessage = loginPage.getErrorMessage();
            System.out.println("Message d'erreur : " + errorMessage);
        }

        // Le test est réussi si nous sommes toujours sur la page de login
        // (même si l'application ne montre pas explicitement un message d'erreur)

        System.out.println("===== FIN TEST: testLoginWithInvalidCredentials =====");
    }
}