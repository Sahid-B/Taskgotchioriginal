package ec.edu.espe.pa.taskgotchi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Bloqueo de accesos (JWT), aislamiento entre usuarios, límite de intentos y aspectos AOP. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "taskgotchi.security.auth-rate-limit.max-attempts=50"})
@ActiveProfiles("h2")
@ExtendWith(OutputCaptureExtension.class)
class SecurityIntegrationTest {

    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    final HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> call(String method, String path, String body, String token, String clientIp) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (clientIp != null) request.header("X-Forwarded-For", clientIp);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    String register(String prefix) throws Exception {
        String user = prefix + System.nanoTime();
        var response = call("POST", "/api/v1/auth/register", json.writeValueAsString(Map.of(
                "username", user, "email", user + "@espe.edu.ec", "password", "Sintetica_2026")), null, null);
        assertEquals(201, response.statusCode());
        return json.readTree(response.body()).get("token").asString();
    }

    String taskBody(String code) throws Exception {
        return json.writeValueAsString(Map.of("code", code, "title", "Mision privada", "estimatedHours", 1,
                "priority", "LOW", "difficulty", "EASY", "active", true));
    }

    @Test
    void blocksRequestsWithoutValidToken() throws Exception {
        var noToken = call("GET", "/api/v1/tasks", null, null, null);
        assertEquals(401, noToken.statusCode());
        assertEquals(6, json.readTree(noToken.body()).size());
        assertThat(noToken.headers().firstValue("WWW-Authenticate")).hasValue("Bearer");

        var forged = call("GET", "/api/v1/tasks", null, "eyJhbGciOiJIUzI1NiJ9.e30.firma-falsa", null);
        assertEquals(401, forged.statusCode());
        assertThat(json.readTree(forged.body()).get("message").asString()).contains("Token inválido");

        assertEquals(200, call("GET", "/v3/api-docs", null, null, null).statusCode());
    }

    @Test
    void eachStudentOnlySeesTheirOwnData(CapturedOutput output) throws Exception {
        String alice = register("alice");
        String bob = register("bob");

        var created = call("POST", "/api/v1/tasks", taskBody("PRIV-" + System.nanoTime()), alice, null);
        assertEquals(201, created.statusCode());
        long id = json.readTree(created.body()).get("id").asLong();

        assertEquals(200, call("GET", "/api/v1/tasks/" + id, null, alice, null).statusCode());
        assertEquals(404, call("GET", "/api/v1/tasks/" + id, null, bob, null).statusCode());
        assertEquals(404, call("PATCH", "/api/v1/tasks/" + id + "/complete", null, bob, null).statusCode());
        assertEquals(200, call("PATCH", "/api/v1/tasks/" + id + "/complete", null, alice, null).statusCode());

        // Aspecto de auditoría de negocio (AOP): evento, IDs y recompensa interna.
        assertThat(output).contains("event=TASK_COMPLETED").contains("args={id=" + id + "}").contains("event=REWARD_GRANTED");
        assertThat(output).contains("event=TASK_COMPLETED user=").contains("result=FAILED reason=ResourceNotFoundException");
    }
}
