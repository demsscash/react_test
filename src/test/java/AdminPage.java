import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
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
    private final By adminTitleSelector = By.cssSelector("h1");
    private final By navigationLinksSelector = By.cssSelector("nav a, header a, .sidebar a");

    // Sélecteurs pour les pages admin spécifiques
    private final By medecinsLinkSelector = By.cssSelector("a[href*='medecins']");
    private final By patientsLinkSelector = By.cssSelector("a[href*='patients']");
    private final By secretairesLinkSelector = By.cssSelector("a[href*='secretaires']");
    private final By cliniquesLinkSelector = By.cssSelector("a[href*='cliniques']");
    private final By kiosksLinkSelector = By.cssSelector("a[href*='kiosks']");

    // Sélecteurs pour les éléments indiquant que nous sommes sur une page admin
    private final By adminIndicatorsSelector = By.cssSelector(
            ".admin-dashboard, .admin-panel, #admin-content, " +
                    "button[role='admin'], [data-admin='true'], " +
                    ".admin-controls, .admin-header"
    );

    // Sélecteurs pour les éléments typiques d'interfaces d'admin
    private final By adminInterfaceSelector = By.cssSelector(
            "table, .data-table, .admin-table, " +
                    ".user-management, .system-config, " +
                    ".admin-tools, .dashboard-stats"
    );

    public AdminPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public AdminPage goToAdminPage() {
        System.out.println("Navigation vers la page admin...");
        driver.get(baseUrl + "/admin");

        // Attendre un moment pour s'assurer que la page a le temps de charger/rediriger
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Afficher les détails de la page
        System.out.println("Navigué vers la page admin, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public AdminPage goToMedecinsPage() {
        System.out.println("Navigation vers la page médecins...");
        driver.get(baseUrl + "/medecins");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Navigué vers la page médecins, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public AdminPage goToPatientsPage() {
        System.out.println("Navigation vers la page patients...");
        driver.get(baseUrl + "/patients");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Navigué vers la page patients, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public AdminPage goToSecretairesPage() {
        System.out.println("Navigation vers la page secrétaires...");
        driver.get(baseUrl + "/secretaires");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Navigué vers la page secrétaires, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public AdminPage goToCliniksPage() {
        System.out.println("Navigation vers la page cliniques...");
        driver.get(baseUrl + "/cliniques");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Navigué vers la page cliniques, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public AdminPage goToKiosksPage() {
        System.out.println("Navigation vers la page kiosks...");
        driver.get(baseUrl + "/kiosks");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Navigué vers la page kiosks, URL actuelle: " + driver.getCurrentUrl());
        return this;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isOnAdminPage() {
        try {
            // 1. Vérifier si l'URL contient "/admin"
            boolean urlContainsAdmin = driver.getCurrentUrl().contains("/admin");
            if (urlContainsAdmin) {
                System.out.println("Détecté sur la page admin via URL");
                return true;
            }

            // 2. Vérifier la présence des liens admin
            boolean hasAdminLinks = areAdminLinksVisible();
            if (hasAdminLinks) {
                System.out.println("Détecté sur la page admin via la présence de liens admin");
                return true;
            }

            // 3. Vérifier la présence d'éléments indicateurs d'admin
            boolean hasAdminIndicators = !driver.findElements(adminIndicatorsSelector).isEmpty();
            if (hasAdminIndicators) {
                System.out.println("Détecté sur la page admin via la présence d'indicateurs admin");
                return true;
            }

            // 4. Vérifier la présence d'éléments d'interface admin typiques
            boolean hasAdminInterface = !driver.findElements(adminInterfaceSelector).isEmpty();
            if (hasAdminInterface) {
                System.out.println("Détecté sur la page admin via la présence d'éléments d'interface admin");
                return true;
            }

            // Si aucune des conditions n'est remplie, nous ne sommes pas sur une page admin
            System.out.println("Pas sur la page admin: URL=" + driver.getCurrentUrl());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de la page admin: " + e.getMessage());
            return false;
        }
    }

    public boolean canAccessMedecinsPage() {
        try {
            goToMedecinsPage();

            // Attendre un moment pour s'assurer que la page est chargée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si nous sommes toujours sur la page médecins ou si nous avons été redirigés
            String currentUrl = driver.getCurrentUrl();
            boolean isOnMedecinsPage = currentUrl.contains("/medecins");
            boolean isOnLoginPage = currentUrl.contains("/login");

            System.out.println("Vérification d'accès à la page médecins - URL: " + currentUrl);

            // Si nous sommes sur la page médecins et pas sur la page login, l'accès est autorisé
            return isOnMedecinsPage && !isOnLoginPage;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification d'accès à la page médecins: " + e.getMessage());
            return false;
        }
    }

    public boolean canAccessPatientsPage() {
        try {
            goToPatientsPage();

            // Attendre un moment pour s'assurer que la page est chargée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si nous sommes toujours sur la page patients ou si nous avons été redirigés
            String currentUrl = driver.getCurrentUrl();
            boolean isOnPatientsPage = currentUrl.contains("/patients");
            boolean isOnLoginPage = currentUrl.contains("/login");

            System.out.println("Vérification d'accès à la page patients - URL: " + currentUrl);

            // Si nous sommes sur la page patients et pas sur la page login, l'accès est autorisé
            return isOnPatientsPage && !isOnLoginPage;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification d'accès à la page patients: " + e.getMessage());
            return false;
        }
    }

    public boolean canAccessSecretairesPage() {
        try {
            goToSecretairesPage();

            // Attendre un moment pour s'assurer que la page est chargée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si nous sommes toujours sur la page secrétaires ou si nous avons été redirigés
            String currentUrl = driver.getCurrentUrl();
            boolean isOnSecretairesPage = currentUrl.contains("/secretaires");
            boolean isOnLoginPage = currentUrl.contains("/login");

            System.out.println("Vérification d'accès à la page secrétaires - URL: " + currentUrl);

            // Si nous sommes sur la page secrétaires et pas sur la page login, l'accès est autorisé
            return isOnSecretairesPage && !isOnLoginPage;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification d'accès à la page secrétaires: " + e.getMessage());
            return false;
        }
    }

    public boolean canAccessCliniquesPage() {
        try {
            goToCliniksPage();

            // Attendre un moment pour s'assurer que la page est chargée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si nous sommes toujours sur la page cliniques ou si nous avons été redirigés
            String currentUrl = driver.getCurrentUrl();
            boolean isOnCliniquesPage = currentUrl.contains("/cliniques");
            boolean isOnLoginPage = currentUrl.contains("/login");

            System.out.println("Vérification d'accès à la page cliniques - URL: " + currentUrl);

            // Si nous sommes sur la page cliniques et pas sur la page login, l'accès est autorisé
            return isOnCliniquesPage && !isOnLoginPage;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification d'accès à la page cliniques: " + e.getMessage());
            return false;
        }
    }

    public boolean canAccessKiosksPage() {
        try {
            goToKiosksPage();

            // Attendre un moment pour s'assurer que la page est chargée
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vérifier si nous sommes toujours sur la page kiosks ou si nous avons été redirigés
            String currentUrl = driver.getCurrentUrl();
            boolean isOnKiosksPage = currentUrl.contains("/kiosks");
            boolean isOnLoginPage = currentUrl.contains("/login");

            System.out.println("Vérification d'accès à la page kiosks - URL: " + currentUrl);

            // Si nous sommes sur la page kiosks et pas sur la page login, l'accès est autorisé
            return isOnKiosksPage && !isOnLoginPage;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification d'accès à la page kiosks: " + e.getMessage());
            return false;
        }
    }

    public boolean isUserRedirectedToUnauthorized() {
        try {
            // Vérifier si l'URL indique une redirection vers une page non autorisée
            String currentUrl = driver.getCurrentUrl();
            boolean isUnauthorized = currentUrl.contains("/unauthorized") ||
                    currentUrl.contains("/forbidden") ||
                    currentUrl.contains("/access-denied") ||
                    (currentUrl.contains("/login") && !currentUrl.contains("?from=")); // Redirection vers login sans référent

            System.out.println("Vérification redirection unauthorized: " + isUnauthorized + ", URL: " + currentUrl);
            return isUnauthorized;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de redirection: " + e.getMessage());
            return false;
        }
    }

    public List<WebElement> getNavigationLinks() {
        try {
            List<WebElement> links = driver.findElements(navigationLinksSelector);
            System.out.println("Nombre de liens de navigation trouvés: " + links.size());

            // Afficher les liens trouvés pour le débogage
            for (int i = 0; i < links.size(); i++) {
                try {
                    String linkText = links.get(i).getText();
                    String linkHref = links.get(i).getAttribute("href");
                    System.out.println("  " + (i+1) + ". Lien: '" + linkText + "', href: " + linkHref);
                } catch (Exception e) {
                    System.out.println("  " + (i+1) + ". Impossible d'obtenir les détails du lien: " + e.getMessage());
                }
            }

            return links;
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des liens de navigation: " + e.getMessage());
            return List.of();
        }
    }

    public boolean areAdminLinksVisible() {
        try {
            // Vérifier la présence des liens admin un par un
            boolean hasMedecinsLink = !driver.findElements(medecinsLinkSelector).isEmpty();
            boolean hasPatientsLink = !driver.findElements(patientsLinkSelector).isEmpty();
            boolean hasSecretairesLink = !driver.findElements(secretairesLinkSelector).isEmpty();
            boolean hasCliniqueLink = !driver.findElements(cliniquesLinkSelector).isEmpty();
            boolean hasKiosksLink = !driver.findElements(kiosksLinkSelector).isEmpty();

            // Log pour débogage
            System.out.println("Liens admin présents - Médecins: " + hasMedecinsLink +
                    ", Patients: " + hasPatientsLink +
                    ", Secrétaires: " + hasSecretairesLink +
                    ", Cliniques: " + hasCliniqueLink +
                    ", Kiosks: " + hasKiosksLink);

            // On considère que les liens sont visibles si au moins un d'entre eux est présent
            return hasMedecinsLink || hasPatientsLink || hasSecretairesLink || hasCliniqueLink || hasKiosksLink;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification des liens admin: " + e.getMessage());
            return false;
        }
    }

    // Méthode pour vérifier si nous sommes redirigés vers login après navigation vers une page
    public boolean isRedirectedToLogin(String pageUrl) {
        try {
            driver.get(pageUrl);

            // Attendre un moment pour la redirection
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String currentUrl = driver.getCurrentUrl();
            boolean redirectedToLogin = currentUrl.contains("/login");

            System.out.println("Vérification redirection vers login depuis " + pageUrl + ": " + redirectedToLogin);

            return redirectedToLogin;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de redirection: " + e.getMessage());
            return false;
        }
    }
}