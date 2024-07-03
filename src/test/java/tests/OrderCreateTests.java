package tests;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import stellarburgers.model.User;
import stellarburgers.steps.OrderSteps;
import stellarburgers.steps.UserSteps;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;


public class OrderCreateTests extends AbstractTest {
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
    @DisplayName("Создание заказа с авторизацией + ингредиенты")
    public void createOrderAuthorizedUserTest() {
        ValidatableResponse loginResponse = userSteps
                .login(user)
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String token = loginResponse.extract().path("accessToken");
        ValidatableResponse ingredientsResponse = orderSteps.getIngredients() // Получение списка ингредиентов
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String firstIngredientId = ingredientsResponse.extract().path("data[0]._id"); // Извлечение id нужных ингредиентов
        String secondIngredientId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredientsList = List.of(firstIngredientId, secondIngredientId); // Создание списка id ингредиентов
        orderSteps.createOrderAuthorized(token, ingredientsList)                // Создание заказа с авторизацией
                .assertThat()
                .statusCode(200)
                .body("name", is("Spicy флюоресцентный бургер"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderNonAuthorizedUserTest() {
        ValidatableResponse ingredientsResponse = orderSteps.getIngredients() // Получение списка ингредиентов
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
        String firstIngredientId = ingredientsResponse.extract().path("data[0]._id"); // Извлечение id нужных ингредиентов
        String secondIngredientId = ingredientsResponse.extract().path("data[4]._id");
        List<String> ingredientsList = List.of(firstIngredientId, secondIngredientId); // Создание списка id ингредиентов
        orderSteps
                .createOrderNonAuthorized((ingredientsList))
                .assertThat()
                .statusCode(401); //заказ почему то создается успешно без токена,
                                   // но тут поставил ожидаемый код 401 Unauthorized, считаю заказ не должен создаваться без авторизации
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
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
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
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

}
