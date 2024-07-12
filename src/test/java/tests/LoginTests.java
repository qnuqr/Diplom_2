package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.UserSteps;
import static org.hamcrest.CoreMatchers.is;


public class LoginTests extends AbstractTest {
    private final UserSteps userSteps = new UserSteps();
    private User user;
    private final Faker faker = new Faker();

    @Before
    public void setUp() {
        user = new User();
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password());
        user.setName(faker.name().firstName());
        userSteps.createUser(user);
    }

    @Test
    @DisplayName("логин под существующим пользователем")
    public void loginUser() {
        userSteps
                .login(user)
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("логин с неверным логином")
    public void loginWithInvalidEmail() {
        user.setEmail("cc");
        userSteps
                .login(user)
                .statusCode(401)
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @DisplayName("логин с неверным паролем")
    public void loginWithInvalidPassword() {
        user.setPassword("123");
        userSteps
                .login(user)
                .statusCode(401)
                .body("message", is("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        String token = userSteps.login(user)
                .extract().body().path("accessToken");
        user.setAccessToken(token);

        if (user.getAccessToken() != null) {
            userSteps.deleteUser(user);
        }
    }

}
