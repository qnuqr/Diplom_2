package tests;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.OrderSteps;
import stellarburgers.steps.UserSteps;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class OrderTests extends AbstractTest {
    private OrderSteps orderSteps = new OrderSteps();
    private UserSteps userSteps = new UserSteps();
    private User user;
    private Faker faker = new Faker();
    List<String> ingredients = List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72");


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
    public void createOrderAuthorizedUserTest() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderSteps
                .createOrderAuthorized(token, (ingredients))
                .assertThat()
                .statusCode(200)
                .body("name", is("Spicy флюоресцентный бургер"));
    }

    @Test
    public void createOrderNonAuthorizedUserTest() {
        orderSteps
                .createOrderNonAuthorized((ingredients))
                .assertThat()
                .statusCode(200)
                .body("name", is("Spicy флюоресцентный бургер"));
    }

    @Test
    public void createOrderNoIngredientTest() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderSteps
                .createOrderNoIngredient(token)
                .assertThat()
                .statusCode(400)
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderIncorrectIngredientTest() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderSteps
                .createOrderAuthorized(token, List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa7"))
                .assertThat()
                .statusCode(500);
    }

    @Test
    public void getAuthorizedUserOrders() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderSteps
                .createOrderAuthorized(token, (ingredients));
        orderSteps
                .getAuthorizedUserOrder(token)
                .assertThat()
                .statusCode(200)
                .body("orders.ingredients.flatten()", is(ingredients));
    }

    @Test
    public void getNonAuthorizedUserOrders() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        orderSteps
                .createOrderAuthorized(token, (ingredients));
        orderSteps
                .getNonAuthorizedUserOrder()
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
