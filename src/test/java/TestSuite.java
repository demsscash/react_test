import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AdminLoginTest.class,
        NonAdminAccessTest.class
})
public class TestSuite {
    // Cette classe reste vide, elle sert uniquement à regrouper les classes de test
}