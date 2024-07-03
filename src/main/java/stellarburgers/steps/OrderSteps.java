package stellarburgers.steps;
import static stellarburgers.Endpoints.*;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class OrderSteps {

    @Step("Получение списка ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_INGREDIENTS)
                .then();
    }

    @Step("Создание заказа с авторизацией")
    public ValidatableResponse createOrderAuthorized(String accessToken, List<String> ingredients) {
        Map<String, Object> requestMap = Map.of("ingredients", ingredients);
        return given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body(requestMap)
                .when()
                .post(CREATE_ORDER)
                .then();
    }

    @Step("Создание заказа без авторизации")
    public ValidatableResponse createOrderNonAuthorized(List<String> ingredients) {
        Map<String, Object> requestMap = Map.of("ingredients", ingredients);
        return given()
                .contentType(ContentType.JSON)
                .body(requestMap)
                .when()
                .post(CREATE_ORDER)
                .then();
    }

    @Step("Создание заказа без ингредиента")
    public ValidatableResponse createOrderNoIngredient(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .post(CREATE_ORDER)
                .then();
    }

    @Step("Получение заказа с авторизацией")
    public ValidatableResponse getAuthorizedUserOrder(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .contentType(ContentType.JSON)
                .when()
                .get(GET_USER_ORDERS)
                .then();
    }

    @Step("Получение заказа без авторизации")
    public ValidatableResponse getNonAuthorizedUserOrder() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_USER_ORDERS)
                .then();
    }

}
