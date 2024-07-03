package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
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


public class GetUserOrderTests extends  AbstractTest {
    private final OrderSteps orderSteps = new OrderSteps();
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
    @DisplayName("Получение заказов авторизованного пользователя")
    public void getAuthorizedUserOrders() {
        ValidatableResponse validatableResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = validatableResponse
                .extract()
                .path("accessToken");
        ValidatableResponse ingredientsResponse = orderSteps.getIngredients() // Получение списка ингредиентов
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String firstIngredientId = ingredientsResponse.extract().path("data[0]._id"); // Извлечение id нужных ингредиентов
        String secondIngredientId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredientsList = List.of(firstIngredientId, secondIngredientId); // Создание списка id ингредиентов
        orderSteps
                .createOrderAuthorized(token, (ingredientsList));
        orderSteps
                .getAuthorizedUserOrder(token)
                .assertThat()
                .statusCode(200)
                .body("orders.ingredients.flatten()", is(ingredientsList));
    }

    @Test
    @DisplayName("Получение заказов не авторизованного пользователя")
    public void getNonAuthorizedUserOrders() {
        userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
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
