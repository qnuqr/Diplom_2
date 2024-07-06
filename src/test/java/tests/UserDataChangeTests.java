package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.UserSteps;
import static org.hamcrest.CoreMatchers.is;


public class UserDataChangeTests extends AbstractTest {
    private final UserSteps userSteps = new UserSteps();
    private User user;
    private final Faker faker = new Faker();

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        user = new User();
        user.setName(faker.name().fullName());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password());
        userSteps.createUser(user);
    }

    @Test
    @DisplayName("Смена почты с токеном")
    public void changeUserEmailTest() {
        String newEmail = faker.internet().emailAddress();
        ValidatableResponse validatableResponse = userSteps
                .userEmailEdit(user, newEmail)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String changedEmail = validatableResponse
                .extract()
                .path("user.email");
        Assert.assertEquals(user.getEmail(), changedEmail);
    }

    @Test
    @DisplayName("Смена имени с токеном")
    public void changeUserNameTest() {
        String newName = faker.name().firstName();
        ValidatableResponse validatableResponse = userSteps
                .userNameEdit(user, newName)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String changedName = validatableResponse
                .extract()
                .path("user.name");
        Assert.assertEquals(user.getName(), changedName);
    }

    @Test
    @DisplayName("Смена имени без токена")
    public void changeUserNameWithoutTokenTest() {
        String newName = faker.name().firstName();
        userSteps
                .userNameEditWithoutToken(user, newName)
                .assertThat()
                .statusCode(401)
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Смена почты без токена")
    public void changeUserEmailWithoutTokenTest() {
        String newEmail = faker.internet().emailAddress();
        userSteps
                .userEmailEditWithoutToken(user, newEmail)
                .assertThat()
                .statusCode(401)
                .body("message", is("You should be authorised"));
    }

    @Test
    @DisplayName("Смена почты, на почту которая уже используется")
    public void changeUserEmailExistTest() {
        String email = "awd@hk.ru";
        userSteps
                .userEmailEdit(user, email)
                .assertThat()
                .statusCode(403)
                .body("message", is("User with such email already exists"));
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
