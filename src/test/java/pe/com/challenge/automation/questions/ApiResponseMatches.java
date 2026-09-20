package pe.com.challenge.automation.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.validators.ApiResponseValidator;

public final class ApiResponseMatches implements Question<Boolean> {
    private final ApiTestData data;

    private ApiResponseMatches(ApiTestData data) {
        this.data = data;
    }

    public static ApiResponseMatches expected(ApiTestData data) {
        return new ApiResponseMatches(data);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        Response response = LastResponse.received().answeredBy(actor);
        String responseBody = response.getBody() == null ? "" : response.getBody().asString();
        ApiResponseValidator.validate(data, response.statusCode(), responseBody);
        return true;
    }

    @Override
    public String getSubject() {
        return "el status y el response coinciden con la configuración del Excel";
    }
}
