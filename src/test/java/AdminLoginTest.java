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

public class AdminLoginTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private AdminPage adminPage;

    // URL avec le port correct
    private final String BASE_URL = "http://localhost:3005";

    // Identifiants d'un administrateur "admin@medical.com", "00001991"
    private final String ADMIN_EMAIL = "admin@medical.com"; // Changez pour un email admin valide
    private final String ADMIN_PASSWORD = "00001991"; // Changez pour un mot de passe admin valide

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
    public void testAdminLoginAndRedirection() {
        // 1. Se connecter avec un compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // 2. Vérifier que l'utilisateur est redirigé vers la page d'accueil (racine)
        String currentUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL));
    }

    @Test
    public void testAdminCanAccessAdminPage() {
        // 1. Se connecter avec un compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // 2. Attendre la redirection vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // 3. Accéder à la page admin
        adminPage.goToAdminPage();

        // 4. Vérifier que l'utilisateur peut accéder à la page admin
        assertTrue("L'utilisateur admin devrait pouvoir accéder à la page admin",
                adminPage.isOnAdminPage());
    }

    @Test
    public void testAdminCanAccessAllAdminPages() {
        // Se connecter d'abord avec le compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Attendre la redirection vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // Vérifier l'accès à toutes les pages admin
        assertTrue("L'admin devrait pouvoir accéder à la page médecins",
                adminPage.canAccessMedecinsPage());

        assertTrue("L'admin devrait pouvoir accéder à la page patients",
                adminPage.canAccessPatientsPage());

        assertTrue("L'admin devrait pouvoir accéder à la page secrétaires",
                adminPage.canAccessSecretairesPage());

        assertTrue("L'admin devrait pouvoir accéder à la page cliniques",
                adminPage.canAccessCliniquesPage());

        assertTrue("L'admin devrait pouvoir accéder à la page kiosks",
                adminPage.canAccessKiosksPage());
    }

    @Test
    public void testAdminNavigationLinks() {
        // Se connecter d'abord avec le compte admin
        loginPage.open();
        loginPage.login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Attendre la redirection vers la page d'accueil
        String homeUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur devrait être redirigé vers la racine après connexion",
                homeUrl.equals(BASE_URL + "/") || homeUrl.equals(BASE_URL));

        // Aller à la page admin
        adminPage.goToAdminPage();

        // Vérifier que les liens de navigation sont visibles
        assertTrue("Les liens de navigation admin devraient être visibles",
                adminPage.areAdminLinksVisible());

        // Vérifier le nombre de liens (peut varier selon votre implémentation)
        int expectedLinkCount = 5; // médecins, patients, secrétaires, cliniques, kiosks
        int actualLinkCount = adminPage.getNavigationLinks().size();

        assertTrue("Le nombre de liens de navigation devrait être d'au moins " + expectedLinkCount +
                ", mais était de " + actualLinkCount, actualLinkCount >= expectedLinkCount);
    }

    @Test
    public void testDirectAccessToAdminPagesWithoutLogin() {
        // Essayer d'accéder directement à une page admin sans être connecté
        adminPage.goToAdminPage();

        // Vérifier qu'on est redirigé vers la page de login
        String currentUrl = driver.getCurrentUrl();
        assertTrue("L'utilisateur non connecté devrait être redirigé vers la page de login",
                currentUrl.contains("/login"));
    }
}