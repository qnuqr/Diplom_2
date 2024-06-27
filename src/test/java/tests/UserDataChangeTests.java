package tests;

import com.github.javafaker.Faker;
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
    private UserSteps userSteps = new UserSteps();
    private User user;
    private Faker faker = new Faker();

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
    public void changeUserNameWithoutTokenTest() {
        String newName = faker.name().firstName();
        userSteps
                .userNameEditWithoutToken(user, newName)
                .assertThat()
                .statusCode(401)
                .body("message", is("You should be authorised"));
    }

    @Test
    public void changeUserEmailWithoutTokenTest() {
        String newEmail = faker.internet().emailAddress();
        userSteps
                .userEmailEditWithoutToken(user, newEmail)
                .assertThat()
                .statusCode(401)
                .body("message", is("You should be authorised"));
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
