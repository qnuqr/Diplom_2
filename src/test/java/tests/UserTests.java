package tests;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.UserSteps;

import static org.hamcrest.CoreMatchers.is;

public class UserTests extends AbstractTest {
    private UserSteps userSteps = new UserSteps();
    private User user;
    private Faker faker = new Faker();

    @Before
    public void setUp() {
        user = new User();
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password());
        user.setName(faker.name().firstName());
    }

    @Test
    public void createUser() {
        userSteps
                .createUser(user)
                .statusCode(200)
                .body("success", is(true));
    }


    @Test
    public void createUserAlreadyRegistered() {
        user.setEmail("test-data@yandex.ru");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("success", is(false));
    }

    @Test
    public void createUserWithoutEmail() {
        user.setEmail("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    public void createUserWithoutName() {
        user.setName("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    public void createUserWithoutPassword() {
        user.setPassword("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    public void loginUser() {
        userSteps
                .createUser(user);
        userSteps
                .login(user)
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    public void loginWithInvalidUsernameNPassword() {
        user.setName("cc");
        user.setPassword("123");
        userSteps
                .login(user)
                .statusCode(401)
                .body("message", is("email or password are incorrect"));
    }


    @After
    public void tearDown() {
        if (user.getAccessToken() != null) {
            String token = userSteps.login(user)
                    .extract().body().path("accessToken");
            user.setAccessToken(token);
            userSteps.
                    deleteUser(user);
        }
    }

}
