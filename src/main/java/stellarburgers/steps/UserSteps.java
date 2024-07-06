package stellarburgers.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import stellarburgers.model.User;
import static io.restassured.RestAssured.given;
import static stellarburgers.Endpoints.*;


public class UserSteps {
    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .log().all() // Логируем все данные запроса
                .body(user)
                .when()
                .post(REGISTER_USER)
                .then()
                .log().all(); // Логируем все данные ответа

    }

    @Step("Авторизация юзера")
    public ValidatableResponse login(User user) {
        return given()
                .body(user)
                .when()
                .post(LOGIN_USER)
                .then();
    }

    @Step("Удаление юзера")
    public ValidatableResponse deleteUser(User user) {
        return given()
                .header("accessToken", user.getAccessToken())
                .when()
                .delete(DELETE_USER)
                .then();
    }

    @Step("Получение токена")
    private static String getToken(User user) {
        ValidatableResponse loginResponse = given()
                .body(user)
                .when()
                .post(LOGIN_USER)
                .then()
                .assertThat()
                .statusCode(200);
        String token = loginResponse.extract().path("accessToken");
        if (token == null) {
            throw new IllegalArgumentException("Token is null");
        }
        return token;
    }

    @Step("Смена поля email")
    public ValidatableResponse userEmailEdit(User user, String newEmail) {
        String token = getToken(user);
        user.setEmail(newEmail);
        return given()
                .header("Authorization", token)
                .body(user)
                .when()
                .patch(UPDATE_USER)
                .then();

    }

    @Step("Смена поля Имя")
    public ValidatableResponse userNameEdit(User user, String newName) {
        String token = getToken(user);
        user.setName(newName);
        return given()
                .header("Authorization", token)
                .body(user)
                .when()
                .patch(UPDATE_USER)
                .then();
    }

    @Step("Смена поля Имя без токена")
    public ValidatableResponse userNameEditWithoutToken(User user, String newName) {
        user.setName(newName);
        return given()
                .body(user)
                .when()
                .patch(UPDATE_USER)
                .then();
    }

    @Step("Смена поля email без токена")
    public ValidatableResponse userEmailEditWithoutToken(User user, String newEmail) {
        user.setEmail(newEmail);
        return given()
                .body(user)
                .when()
                .patch(UPDATE_USER)
                .then();
    }
}
