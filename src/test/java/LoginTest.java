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

public class LoginTest {

    private WebDriver driver;
    private LoginPage loginPage;
    // URL mise à jour avec le bon port
    private final String BASE_URL = "http://localhost:3005";

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
            // Utiliser WebDriverManager pour télécharger et configurer ChromeDriver
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            // Ajouter des options pour éviter les problèmes courants
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            // Décommentez la ligne suivante pour exécuter en mode headless
            // options.addArguments("--headless=new");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();

            loginPage = new LoginPage(driver, BASE_URL);

            // Créer le dossier screenshots s'il n'existe pas
            try {
                Files.createDirectories(Paths.get("screenshots"));
            } catch (IOException e) {
                System.err.println("Erreur lors de la création du dossier screenshots: " + e.getMessage());
            }

            // Accéder à la page de connexion
            loginPage.open();
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
    public void testLoginPageElements() {
        // Vérifier le titre de la page
        String titleText = loginPage.getTitle();
        assertTrue("Le titre devrait contenir 'Se connecter'", titleText.contains("Se connecter"));

        // Vérifier que les champs du formulaire sont présents et visibles
        assertTrue("Le champ email devrait être visible", loginPage.getEmailInput().isDisplayed());
        assertTrue("Le champ mot de passe devrait être visible", loginPage.getPasswordInput().isDisplayed());
        assertTrue("Le bouton de connexion devrait être visible", loginPage.getSubmitButton().isDisplayed());

        // Vérifier les placeholders
        assertEquals("email", loginPage.getEmailInput().getAttribute("placeholder"));
        assertEquals("password", loginPage.getPasswordInput().getAttribute("placeholder"));
    }

    @Test
    public void testEmailFieldHasFocus() {
        // Vérifier que le champ email a le focus
        assertEquals("username", loginPage.getActiveElementId());
    }

    @Test
    public void testInvalidCredentials() {
        // Tenter de se connecter avec des identifiants incorrects
        loginPage.login("utilisateur@test.com", "mauvais_mot_de_passe");

        // Attendre et vérifier le message d'erreur
        String errorText = loginPage.getErrorMessage();

        assertTrue(
                "Le message d'erreur devrait indiquer des identifiants invalides",
                errorText.contains("Email ou mot de passe invalide") ||
                        errorText.contains("No Server Response")
        );
    }

    @Test
    public void testErrorDisappearsOnInputChange() {
        // Provoquer d'abord une erreur
        loginPage.login("erreur@test.com", "mot_de_passe_incorrect");

        // Attendre que le message d'erreur apparaisse
        loginPage.waitForErrorMessage();

        // Modifier le champ email
        loginPage.fillLoginForm("nouvelle@valeur.com", "");

        // Vérifier que le message d'erreur a disparu
        loginPage.waitForErrorToDisappear();

        assertFalse("Le message d'erreur devrait être masqué", loginPage.isErrorVisible());
    }

    @Test
    public void testSuccessfulLogin() {
        // Remplacez ces valeurs par des identifiants valides pour votre application
        loginPage.login("admin@medical.com", "00001991");

        try {
            // Attendre la redirection
            loginPage.waitForRedirect();

            // Vérifier que nous avons bien été redirigés
            String currentUrl = loginPage.getCurrentUrl();
            assertFalse("L'utilisateur devrait être redirigé après connexion", currentUrl.contains("/login"));
        } catch (Exception e) {
            // Prendre une capture d'écran en cas d'erreur
            captureScreenshot("redirect_error");
            throw e;
        }
    }

    @Test
    public void testEmailValidation() {
        // Tenter de se connecter avec un email mal formaté
        loginPage.login("email_non_valide", "mot_de_passe");

        // Vérifier que nous sommes toujours sur la page de connexion
        String currentUrl = loginPage.getCurrentUrl();
        assertTrue("L'utilisateur devrait rester sur la page de connexion", currentUrl.contains("/login"));
    }
}