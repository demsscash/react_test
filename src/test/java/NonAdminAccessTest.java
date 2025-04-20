import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.Assert.*;

public class NonAdminAccessTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private AdminPage adminPage;

    // URL avec le port correct
    private final String BASE_URL = "http://localhost:3005";

    // Identifiants d'un utilisateur standard (non-admin)
    private final String USER_EMAIL = "user@example.com"; // Changez pour un email utilisateur valide
    private final String USER_PASSWORD = "user_password"; // Changez pour un mot de passe utilisateur valide

    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            if (driver != null) {
                captureScreenshot(description.getMethodName());
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
    public void testNonAdminLoginAndRedirection() {
        // Se connecter avec un compte utilisateur standard
        loginPage.open();
        loginPage.login(USER_EMAIL, USER_PASSWORD);

        // Vérifier que l'utilisateur est redirigé vers la page d'accueil (racine)
        String currentUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL));
    }

    @Test
    public void testNonAdminCannotAccessAdminPages() {
        // 1. Se connecter avec un compte utilisateur standard
        loginPage.open();
        loginPage.login(USER_EMAIL, USER_PASSWORD);

        // 2. Vérifier que l'utilisateur est redirigé vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // 3. Essayer d'accéder à la page admin
        adminPage.goToAdminPage();

        // 4. Vérifier que l'utilisateur est redirigé vers la page unauthorized ou ne peut pas accéder à la page admin
        boolean isUnauthorized = adminPage.isUserRedirectedToUnauthorized() ||
                !adminPage.isOnAdminPage();

        assertTrue("L'utilisateur standard ne devrait pas pouvoir accéder à la page admin",
                isUnauthorized);
    }

    @Test
    public void testNonAdminCannotAccessAdminSubpages() {
        // Se connecter d'abord avec le compte utilisateur standard
        loginPage.open();
        loginPage.login(USER_EMAIL, USER_PASSWORD);

        // Attendre la redirection vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // Vérifier que l'utilisateur ne peut pas accéder aux pages admin
        assertFalse("L'utilisateur standard ne devrait pas pouvoir accéder à la page médecins",
                adminPage.canAccessMedecinsPage());

        assertFalse("L'utilisateur standard ne devrait pas pouvoir accéder à la page patients",
                adminPage.canAccessPatientsPage());

        assertFalse("L'utilisateur standard ne devrait pas pouvoir accéder à la page secrétaires",
                adminPage.canAccessSecretairesPage());

        assertFalse("L'utilisateur standard ne devrait pas pouvoir accéder à la page cliniques",
                adminPage.canAccessCliniquesPage());

        assertFalse("L'utilisateur standard ne devrait pas pouvoir accéder à la page kiosks",
                adminPage.canAccessKiosksPage());
    }

    @Test
    public void testRegularUserCanAccessAuthorizedPages() {
        // Se connecter avec un compte utilisateur standard
        loginPage.open();
        loginPage.login(USER_EMAIL, USER_PASSWORD);

        // Attendre la redirection vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // Vérifier l'accès aux pages autorisées pour les utilisateurs standards
        // Vérifier l'accès à la page des factures
        driver.get(BASE_URL + "/factures");
        String currentUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur standard devrait pouvoir accéder à la page des factures",
                currentUrl.contains("/factures"));

        // Vérifier l'accès à la page des ordonnances
        driver.get(BASE_URL + "/ordonnances");
        currentUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur standard devrait pouvoir accéder à la page des ordonnances",
                currentUrl.contains("/ordonnances"));
    }
}