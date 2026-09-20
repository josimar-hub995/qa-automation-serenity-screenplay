package pe.com.challenge.automation.tasks;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.Delete;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.serenitybdd.screenplay.rest.interactions.Patch;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.screenplay.rest.interactions.Put;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import pe.com.challenge.automation.builders.ApiRequestBuilder;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.models.PreparedApiRequest;

import java.net.URI;
import java.net.URISyntaxException;
public class ExecuteApiRequest implements Task {
    private final ApiTestData data;

    public ExecuteApiRequest(ApiTestData data) {
        this.data = data;
    }

    public static ExecuteApiRequest configuredWith(ApiTestData data) {
        return Tasks.instrumented(ExecuteApiRequest.class, data);
    }

    @Override
    @Step("{0} ejecuta la petición API definida en el Excel")
    public <T extends Actor> void performAs(T actor) {
        PreparedApiRequest request = ApiRequestBuilder.from(data);
        EndpointParts endpoint = splitEndpoint(request.endpoint());
        actor.can(CallAnApi.at(endpoint.baseUrl()));

        ScenarioManager.recordTargetUrl(request.endpoint());
        ScenarioManager.recordApiRequest(request);

        switch (request.method()) {
            case "GET" -> actor.attemptsTo(Get.resource(endpoint.resource())
                    .with(specification -> configure(specification, request)));
            case "POST" -> actor.attemptsTo(Post.to(endpoint.resource())
                    .with(specification -> configure(specification, request)));
            case "PUT" -> actor.attemptsTo(Put.to(endpoint.resource())
                    .with(specification -> configure(specification, request)));
            case "PATCH" -> actor.attemptsTo(Patch.to(endpoint.resource())
                    .with(specification -> configure(specification, request)));
            case "DELETE" -> actor.attemptsTo(Delete.from(endpoint.resource())
                    .with(specification -> configure(specification, request)));
            default -> throw new TestDataException("REQUEST_TYPE no soportado: " + request.method());
        }

        Response response = LastResponse.received().answeredBy(actor);
        String responseBody = response.getBody() == null ? "" : response.getBody().asString();
        ScenarioManager.recordApiResponse(response.statusCode(), responseBody);
    }

    private static RequestSpecification configure(
            RequestSpecification specification,
            PreparedApiRequest request) {
        RequestSpecification configured = specification;
        if (!request.headers().isEmpty()) {
            configured = configured.headers(request.headers());
        }
        if (!request.params().isEmpty()) {
            configured = configured.queryParams(request.params());
        }
        if (!request.body().isBlank() && !"{}".equals(request.body())) {
            configured = configured.body(request.body());
        }
        return configured;
    }

    private static EndpointParts splitEndpoint(String endpoint) {
        try {
            URI uri = new URI(endpoint);
            String baseUrl = uri.getScheme() + "://" + uri.getRawAuthority();
            String resource = uri.getRawPath();
            if (resource == null || resource.isBlank()) {
                resource = "/";
            }
            if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
                resource += "?" + uri.getRawQuery();
            }
            return new EndpointParts(baseUrl, resource);
        } catch (URISyntaxException error) {
            throw new TestDataException("ENDPOINT no contiene una URL válida: " + endpoint, error);
        }
    }

    private record EndpointParts(String baseUrl, String resource) {
    }
}
