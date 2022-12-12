package com.everis.ct.web.jira;

import com.everis.ct.web.service.constans.Constants;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

import static com.everis.ct.web.service.io.ManageFiles.getJsonContentAsString;
import static com.everis.ct.web.service.io.ManageFiles.readAsString;
import static com.everis.ct.web.service.util.UtilWeb.logger;

@Component
public class JiraXrayIntegration extends JiraProperties {

    private final String cucumberJsonPath = System.getProperty("user.dir") + "%s";

    public void importJXrayExecutionResults(String cucumberJsonPath) {
        if (jxrayIntegration) {
            if (jxrayPropertiesDefined()) {
                logger(JiraProperties.class).warning(readAsString("logs/log-jira-properties-notdefined.txt"));
            } else {
                RestAssured.useRelaxedHTTPSValidation();
                Response jwt = getJWTTokenAuth();
                if (jwt.getStatusCode() != 200) {
                    logger(JiraXrayIntegration.class).log(Level.WARNING, "Ocurrio un error al generar el token de autentificacion: {0}",
                            jwt.getStatusLine());
                } else {
                    logger(JiraXrayIntegration.class).log(Level.INFO, "Importando resultados a Jira Xray >>> {0}", jiraHost);
                    var jwtToken = jwt.body().asString().replace("\"", "");
                    importResults(jwtToken, cucumberJsonPath);
                }
            }
        } else {
            logger(JiraXrayIntegration.class).info("Integracion con Jira: " + jxrayIntegration);
        }
    }

    private Response getJWTTokenAuth() {
        return RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body(String.format(readAsString("req/req-jwt-auth-jira.json"), clientId, clientSecret))
                .when().log().all()
                .post(jiraHost + jiraAuthEndPoint);
    }

    private void importResults(String jwtToken, String cucumberJsonPath) {
        String jsonContentAsString;
        var defaultCucumberJsonPath = "/target/build/cucumber.json";
        if (cucumberJsonPath.equals(Constants.NOT_DEFINED))
            jsonContentAsString = getJsonContentAsString(String.format(cucumberJsonPath, defaultCucumberJsonPath));
        else
            jsonContentAsString = getJsonContentAsString(String.format(cucumberJsonPath, cucumberJsonPath));

        RequestSpecification req = RestAssured
                .given()
                .header(new Header("Content-Type", "application/json"))
                .header(new Header("Authorization", "Bearer " + jwtToken));
        req.log().uri();
        req.log().headers();
        Response response = req
                .body(jsonContentAsString)
                .when()
                .post(jiraHost + jiraCucumberImportEndPoint);
        response.prettyPeek();

        if (response.getStatusCode() == 200)
            logger(JiraXrayIntegration.class).log(Level.INFO, "Test Execution --> {0}",
                    response.body().jsonPath().getString("key"));
        else
            logger(JiraXrayIntegration.class).log(Level.WARNING, "Ocurrio un error en la respuesta de Xray {0}",
                    response.getStatusCode());
    }

}
