package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.UserSteps;
import static org.hamcrest.CoreMatchers.is;


public class UserCreateTests extends AbstractTest {
    private final UserSteps userSteps = new UserSteps();
    private User user;
    private final Faker faker = new Faker();

    @Before
    public void setUp() {
        user = new User();
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password());
        user.setName(faker.name().firstName());
    }

    @Test
    @DisplayName("Создание юзера")
    public void createUser() {
        userSteps
                .createUser(user)
                .statusCode(200)
                .body("success", is(true));
    }


    @Test
    @DisplayName("Создание юзера который уже зарегистрирован")
    public void createUserAlreadyRegistered() {
        user.setEmail("test-data@yandex.ru");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("success", is(false));
    }

    @Test
    @DisplayName("Создание юзера без почты")
    public void createUserWithoutEmail() {
        user.setEmail("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание юзера без Имени")
    public void createUserWithoutName() {
        user.setName("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание юзера без пароля")
    public void createUserWithoutPassword() {
        user.setPassword("");
        userSteps
                .createUser(user)
                .statusCode(403)
                .body("message", is("Email, password and name are required fields"));
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
